package uy.edu.um.doors;

import exceptions.DatoDuplicadoException;
import exceptions.UsuarioNoEncontradoException;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.list.Node;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.EmptyStackException;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class ProcessManagerImpl implements ProcessManager{
    MyQueue<DoorProcess> new_processes=new MyQueueImpl<>();
    MyHeap<DoorProcess> pending_processes=new MyHeapImpl<>(false); //significa que es heap max. Arregle tambien el compareto
    MyStack<DoorProcess> finished_processes=new MyStackImpl<>();
    private DoorProcess runningProcess;
    private final Logger logger = new Logger();

    //Implementamos hash para busqueda mas rapida ya que usamos ID muy grandes
    private MyHashImpl<Integer,User> userByUID =new MyHashImpl<>();
    private MyHashImpl<Integer,DoorProcess> processesByPID = new MyHashImpl<>();




    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        try {
            if (processesByPID.size() > 0 || userByUID.size() > 0) {
                System.out.println("Error: Los datos ya fueron cargados en esta sesión.");
                return;
                //Esto salta cuando es la segunda vez que haces load, se maneja asi y no
                //como exception para que no vaya a los catch y haga reiniciar estructuras
                //que si pasara eso se borraría el load inicial
            }

            leerUsuarios(usersCsvPath);
            leerProcesos(processCsvPath);

        } catch (IOException e) {
            reiniciarEstructuras();
            System.out.println("Error cargando archivos: " + e.getMessage());

        } catch (DatoDuplicadoException e) {
            reiniciarEstructuras();
            System.out.println("Error de datos duplicados: " + e.getMessage());

        } catch (UsuarioNoEncontradoException e) {
            reiniciarEstructuras();
            System.out.println("Error de usuario no encontrado: " + e.getMessage());
        }
    }

    private void leerUsuarios(String usersPath) throws IOException, DatoDuplicadoException {
        //Modifico el tutorial de archivos dado en clase de informática:
        BufferedReader br = null;
        try {
            br = Files.newBufferedReader(Path.of(usersPath), StandardCharsets.UTF_8);
            String linea = br.readLine(); // leo encabezado: uid;alias;type

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(";"); //En el Csv de usuarios los datos están separados por ";"

                int UID = Integer.parseInt(datos[0]); //Guardo el UID como lo primero antes del ";"
                String alias = datos[1]; //Guardo el alias como lo segundo que hay entre ";"
                User.UserType tipo = User.UserType.valueOf(datos[2]); //Guardo el tipo como lo tercero que hay entre ";"

                if (userByUID.contains(UID)) {
                    throw new DatoDuplicadoException("Usuario repetido con UID: " + UID);
                }

                User user = new User(UID, alias, tipo); //Creo el usuario con los datos que guarde
                userByUID.put(UID, user); //También agrego el UID al Hash
            }
        } finally {
            if(br != null) {
                br.close();
            }
        }
    }

    private void leerProcesos(String processPath) throws IOException, DatoDuplicadoException, UsuarioNoEncontradoException {
        //Modifico el tutorial de archivos dado en clase de informática:
        BufferedReader br = null;
        try {
            br = Files.newBufferedReader(Path.of(processPath), StandardCharsets.UTF_8);
            String linea = br.readLine(); // leo encabezado: pid;uid;name;events

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(";", 4); //El 4 aclara que cuando llegue a una cuarta columna (eventos) deje de dividir

                int PID = Integer.parseInt(datos[0]);
                int UID = Integer.parseInt(datos[1]);
                String nombre = datos[2];
                String eventosTexto = datos[3];

                if (processesByPID.contains(PID)) {
                    throw new DatoDuplicadoException("Proceso repetido con PID: " + PID);
                }

                User propietario = userByUID.get(UID); //Con el UID que conseguí obtengo el objeto User con ese UID

                if (propietario == null) {
                    throw new UsuarioNoEncontradoException(
                            "No existe usuario con UID: " + UID + " para el proceso PID: " + PID
                    );
                }

                MyList<Event> eventos = leerEventos(eventosTexto); //Hay que leer la columna de eventos también

                DoorProcess proceso = new DoorProcess(PID, nombre, propietario, eventos); //Creo el objeto DoorProcess
                new_processes.enqueue(proceso); //Agrego el proceso a la lista de procesos nuevos
                processesByPID.put(PID, proceso); //Agrego el PID a su Hash
            }
        }finally {
            if(br != null) {
                br.close();
            }
            //El if y crearlo = a null al principio es por si por algún error no se llega a crear el br
            //El finally es necesario por si salta algún exception es necesario que se cierre el br, cosa que sin el finally capaz no pasaría
        }
    }

    private void reiniciarEstructuras() {
        new_processes = new MyQueueImpl<>();
        pending_processes = new MyHeapImpl<>(false);
        finished_processes = new MyStackImpl<>();

        runningProcess = null;

        userByUID = new MyHashImpl<>();
        processesByPID = new MyHashImpl<>();

        //Si se empiezan a cargar los datos y se encuentra un repetido la carga se detiene
        //Pero hay que borrar lo que ya había cargado parcialmente, para eso se usa esta función
        //Apunta nuestras estructuras a cosas vacías y lo que teníamos antes queda perdido y lo
        //borra el garbage collector
    }

    private MyList<Event> leerEventos(String eventosTexto) {

        MyList<Event> eventos = new MyLinkedListImpl<>(); //Creo la lista donde voy a ir guardando los eventos

        eventosTexto = eventosTexto.trim(); //Saco espacios de más
        eventosTexto = eventosTexto.substring(1, eventosTexto.length() - 1); //Saco las llaves

        String[] eventosSeparados = eventosTexto.split("#"); //Separo los eventos, divididos por #

        for (String eventoTexto : eventosSeparados) {

            eventoTexto = eventoTexto.trim();
            String[] partes = eventoTexto.split(":", 2); //Separo tipo e instrucciones (dos columnas separadas por :)

            String tipoTexto = partes[0].trim(); //La primero columna es el tipo
            String instruccionesTexto = partes[1].trim(); //La segunda columna son las instrucciones

            Event.EventType tipo = Event.EventType.valueOf(tipoTexto); //Guardo el tipo

            instruccionesTexto = instruccionesTexto.substring(1, instruccionesTexto.length() - 1); // Saco los paréntesis rectos

            // Ahora me quedan las instrucciones separadas por coma
            String[] instruccionesSeparadas = instruccionesTexto.split(","); //Separo las instrucciones

            MyList<String> instrucciones = new MyLinkedListImpl<>(); //Creo la lista donde las voy a guardar

            for (String instruccionesSeparada : instruccionesSeparadas) {
                instrucciones.add(instruccionesSeparada.trim()); //Cada instruccion que había dividido la agrego la lista de instrucciones
            }

            Event evento = new Event(tipo, instrucciones); //Creo el evento
            eventos.add(evento); //Lo agrego a la lista
        }
        return eventos;
    }


    @Override
    public void prepareProcesses() throws Exception {
        //Aca vamos a tener que agarrar todos los procesos nuevos, de ahi calcular su prioridad y luego
        //mover al heap de estado pendiente
        if (new_processes.isEmpty()) {
            throw new Exception("No hay proceso nuevos");
        }
        while (!new_processes.isEmpty()) {
            DoorProcess proceso = new_processes.dequeue();
            proceso.setPrioridad(proceso.calcularPrioridad()); //Preguntar esto, ver como es comparable FLOAT
            proceso.setEstado(DoorProcess.ProcessState.PENDING);
            pending_processes.insert(proceso);
            String mensaje = "[" + logger.getTimestamp() + "]: NEW PENDING PROCESS: PID=" + proceso.getPID() + " | " + proceso.getNombre() + " | USER:" + proceso.getPropietario().getAlias() + " UID:" + proceso.getPropietario().getUID() + " | P=" + proceso.getPrioridad() + "\n";
            logger.write(mensaje);  //Aca utilizo la funcion write
        }
        System.out.println("Se han cargado los nuevos procesos en pending");
    }

    @Override
    public void executeNextProcess(){
        //solo puede existir un proceso en ejecucion en simultaneo, esto significa que runningprocess es un atributo
        //registrar en el log cada vez que comienza un proceso
        //su informacion y de sus eventos asociados.
        if(runningProcess != null) {
            System.out.println("Ya existe otro proceso corriendo, PID: " + runningProcess.getPID());
            return;
        }
        if (pending_processes.isEmpty()) {
            System.out.println("No hay procesos pendientes para ejecutar.");
            return;
        }

        DoorProcess proceso = pending_processes.remove();
        proceso.setEstado(DoorProcess.ProcessState.RUNNING);
        runningProcess = proceso;


        //el string builder construye el mensaje antes de enviarselo al logger porque son muchas lineas
        // y tendria q usar muchos writes seguidos
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("[").append(logger.getTimestamp()).append("]: EXECUTING PROCESS: ")
                .append("PID=").append(proceso.getPID())
                .append(" | USER:").append(proceso.getPropietario().getAlias())
                .append(" UID:").append(proceso.getPropietario().getUID())
                .append("\n");

        Node<Event> node = proceso.getEventosAsociados().getFirst();
        while (node != null) {
            Event evento = node.getValue();
            logEntry.append("EVENT: ").append(evento.getTipo()).append(" | Instructions [");

            Node<String> instrNode = evento.getInstrucciones().getFirst();
            while (instrNode != null) {
                logEntry.append(instrNode.getValue());
                if (instrNode.getNext() != null) { //este if funciona para que no agregue una , en la ultima instruccion
                    logEntry.append(", ");
                }
                instrNode = instrNode.getNext();
            }
            logEntry.append("]\n");
            node = node.getNext();
        }
        //usando nodos es O(n) si uso get(i) termina sendo O(nˆ2)
    // le agregue tambien una recorrida (usando nodos) de la lista de instrucciones dentro de cada evento.

        // Escribir en el archivo de log y mostrar por pantalla
        logger.write(logEntry.toString());
        System.out.print(logEntry.toString());

    }

    // funcion auxiliar porque este codigo se repetia en los 3 finish
    private void stackOverflow() throws EmptyStackException {
        if (finished_processes.size() == DoorProcess.getMaxFinished()) {
            logger.write("[" + logger.getTimestamp() + "]: Finished process stack overflow\n");
            while (!finished_processes.isEmpty()) {
                DoorProcess p = finished_processes.pop();
                processesByPID.remove(p.getPID());

                StringBuilder msg = new StringBuilder();
                msg.append("[").append(p.getFinishedAt()).append("]: ")
                        .append("PID=").append(p.getPID())
                        .append(" ").append(p.getNombre())
                        .append(" | STATE: ").append(p.getfinishedState());

                if (p.getTerminadoPor() != null) {
                    msg.append(" by USER:").append(p.getTerminadoPor().getAlias())
                            .append(" UID:").append(p.getTerminadoPor().getUID());
                }

                msg.append(" | USER:").append(p.getPropietario().getAlias())
                        .append(" UID:").append(p.getPropietario().getUID())
                        .append("\n");

                logger.write(msg.toString());
            }
        }
    }


    @Override
    public void finishProcessOk() throws EmptyStackException {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecución.");
            return;
        }

        DoorProcess proceso = runningProcess;
        runningProcess = null;
        proceso.setEstado(DoorProcess.ProcessState.FINISHED);
        proceso.setFinishedAt(logger.getTimestamp());
        proceso.setFinishedState(DoorProcess.FinishedState.OK);

        stackOverflow();

        finished_processes.push(proceso);
        logger.write("[" + logger.getTimestamp() + "]: ENDING PROCESS: PID=" + proceso.getPID() + " | STATE: OK\n");
    }

    @Override
    public void finishProcessError() throws EmptyStackException {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecución.");
            return;
        }

        DoorProcess proceso = runningProcess;
        runningProcess = null;
        proceso.setEstado(DoorProcess.ProcessState.FINISHED);
        proceso.setFinishedAt(logger.getTimestamp());
        proceso.setFinishedState(DoorProcess.FinishedState.ERROR);
        stackOverflow();

        finished_processes.push(proceso);
        logger.write("[" + logger.getTimestamp() + "]: ENDING PROCESS: PID=" + proceso.getPID() + " | STATE: ERROR\n");
    }

    @Override
    public void terminateProcess(int uid) throws EmptyStackException {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecución.");
            return;
        }

        User terminadoPor = userByUID.get(uid);
        if (terminadoPor == null) {
            System.out.println("No existe usuario con UID=" + uid);
            return;
        }

        DoorProcess proceso = runningProcess;
        runningProcess = null;
        proceso.setEstado(DoorProcess.ProcessState.FINISHED);
        proceso.setFinishedAt(logger.getTimestamp());
        proceso.setFinishedState(DoorProcess.FinishedState.TERMINATED);
        proceso.setTerminadoPor(terminadoPor);

        stackOverflow();

        finished_processes.push(proceso);
        logger.write("[" + logger.getTimestamp() + "]: ENDING PROCESS: PID=" + proceso.getPID() + " | STATE: TERMINATED by USER:" + proceso.getTerminadoPor().getAlias() + " UID:" + proceso.getTerminadoPor().getUID() + "\n");
    }

    //Verbose true si hay que poner eventos
    //-1 si el filtro no aplica
    @Override
    public void printStatus() {
        printStatusAux(false, null);
    }

    @Override
    public void printStatusVerbose() {
        printStatusAux(true, null);
    }

    @Override
    public void printStatusByUser(int uid) {
        printStatusAux(false, uid);
    }

    @Override
    public void printStatusByProcess(int pid) {
        DoorProcess p = processesByPID.get(pid);
        if (p == null) {
            System.out.println("No existe proceso con PID=" + pid);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("PROCESS STATUS\n");
        if (p.getEstado() == DoorProcess.ProcessState.FINISHED) {
            sb.append("FINISHED:\n");
            sb.append("\t").append(formatFinishedProcess(p)).append("\n");
        } else if (p.getEstado() == DoorProcess.ProcessState.RUNNING) {
            sb.append("EXECUTING:\n");
            sb.append("\t").append(formatProcess(p)).append("\n");
        } else if (p.getEstado() == DoorProcess.ProcessState.PENDING) {
            sb.append("PENDING:\n");
            sb.append("\t").append(formatProcess(p)).append("\n");
        } else {
            sb.append("NEW:\n");
            sb.append("\t").append(formatProcess(p)).append("\n");
        }
        appendEvents(sb, p);
        System.out.print(sb.toString());
    }

    private void printStatusAux(boolean verbose, Integer uidFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROCESS STATUS\n");

        //El proceso que se está ejecutando es solo 1, asi que no se necesita función recursiva
        sb.append("EXECUTING:\n");
        if (runningProcess != null && matchesFilters(runningProcess, uidFilter)) {
            //Si hay un proceso ejecutándose y cumple los filtros agregarlo al sb
            sb.append("\t") //Es el tab
                    .append(formatProcess(runningProcess)) //Se tiene que ver con un formato que hice una auxiliar porque comparte con pendientes
                    .append("\n");
            //Si le pusimos como condición que tiene que ser verbose tiene que agregar los eventos de cada proceso
            if (verbose) {
                appendEvents(sb, runningProcess); //Función auxiliar para imprimir los eventos más abajo
            }
        }
        sb.append("PENDING:\n");
        logPendingProcesses(sb, verbose, uidFilter); //Función recursiva para procesos pendientes

        sb.append("FINISHED:\n");
        logFinishedProcesses(sb, verbose, uidFilter); //Función recursiva para procesos finalizados
        System.out.print(sb.toString()); //Lo muestra en consola
    }

    // Función para ver si cumple los filtros
    //Se usa en un if, entonces si es true pone ese proceso, si es false no lo pone
    private boolean matchesFilters(DoorProcess p, Integer uidFilter) {
        //Si hay condición de UID: el UID que se pasó tiene que ser igual al del proceso actual
        return (uidFilter == null && p.getPropietario().getUID() == uidFilter);
    }

    private void logPendingProcesses(StringBuilder sb, boolean verbose, Integer uidFilter) {
        if (pending_processes.isEmpty()) {
            return;
        }
        DoorProcess p = pending_processes.remove(); //Saca un elemento del heap para poder recorrerlo
        if (matchesFilters(p, uidFilter)) {
            sb.append("\t")
                    .append(formatProcess(p))
                    .append("\n");
            if (verbose) {
                appendEvents(sb, p);
            }
        }
        logPendingProcesses(sb, verbose, uidFilter); //Recursion
        pending_processes.insert(p); //Vuelve a agregar el elemento que saco
        //Va a quedar como estaba por el stack de la recursion
    }

    private void logFinishedProcesses(StringBuilder sb, boolean verbose, Integer uidFilter) {
        if (finished_processes.isEmpty()) {
            return;
        }
        try{
            DoorProcess p = finished_processes.pop();
            if (matchesFilters(p, uidFilter)) {
                sb.append("\t")
                        .append(formatFinishedProcess(p))
                        .append("\n");
                if (verbose) {
                    appendEvents(sb, p);
                }
            }
            logFinishedProcesses(sb, verbose, uidFilter);
            finished_processes.push(p);
        }catch(EmptyStackException e){
             return; //Lo tuve que poner para que no me dé error el pop
        }

    }

    private String formatProcess(DoorProcess p) {
        return "PID=" + p.getPID()
                + " | " + p.getNombre()
                + " | USER:" + p.getPropietario().getAlias()
                + " UID:" + p.getPropietario().getUID()
                + " | P=" + p.getPrioridad();
    }

    private String formatFinishedProcess(DoorProcess p) {
        StringBuilder sb = new StringBuilder();
        sb.append("PID=").append(p.getPID())
                .append(" ").append(p.getNombre())
                .append(" | STATE: ").append(p.getfinishedState());

        if (p.getTerminadoPor() != null) {
            sb.append(" by USER:").append(p.getTerminadoPor().getAlias())
                    .append(" UID:").append(p.getTerminadoPor().getUID());
        }

        sb.append(" | USER:").append(p.getPropietario().getAlias())
                .append(" UID:").append(p.getPropietario().getUID());

        return sb.toString();
    }

    //Cuando es verbose necesito poder agregar los eventos
    private void appendEvents(StringBuilder sb, DoorProcess p) {
        Node<Event> eventoNode = p.getEventosAsociados().getFirst();
        while (eventoNode != null) {
            Event evento = eventoNode.getValue();
            sb.append("\t\tEVENT: ")
                    .append(evento.getTipo())
                    .append(" | Instructions [");

            Node<String> instrNode = evento.getInstrucciones().getFirst();
            while (instrNode != null) {
                sb.append(instrNode.getValue());
                if (instrNode.getNext() != null) {
                    sb.append(", ");
                }
                instrNode = instrNode.getNext();
            }
            sb.append("]\n");
            eventoNode = eventoNode.getNext();
        }
    }

}
