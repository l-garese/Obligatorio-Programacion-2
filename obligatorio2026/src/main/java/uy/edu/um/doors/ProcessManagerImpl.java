package uy.edu.um.doors;

import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProcessManagerImpl implements ProcessManager{
    MyQueue<DoorProcess> new_processes=new MyQueueImpl();
    MyHeap<DoorProcess> pending_processes=new MyHeapImpl();
    MyStack<DoorProcess> finished_processes=new MyStackImpl<>();
    private DoorProcess runningProcess;

    //Implementamos hash para busqueda mas rapida ya que usamos ID muy grandes
    private MyHashImpl<Integer,User> userByUID;
    private MyHashImpl<Integer,DoorProcess> processesByPID;


    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        try {
            leerUsuarios(usersCsvPath);
            leerProcesos(processCsvPath);
        } catch (IOException e) {
            System.out.println("Error cargando archivos: " + e.getMessage());
        }
    }

    private void leerUsuarios(String usersPath) throws IOException {
        //Modifico el tutorial de archivos dado en clase de informática:

        BufferedReader br = Files.newBufferedReader(Path.of(usersPath), StandardCharsets.UTF_8);
        String linea = br.readLine(); // leo encabezado: uid;alias;type

        while ((linea = br.readLine()) != null) {

            String[] datos = linea.split(";"); //En el Csv de usuarios los datos están separados por ";"

            int UID = Integer.parseInt(datos[0]); //Guardo el UID como lo primero antes del ";"
            String alias = datos[1]; //Guardo el alias como lo segundo que hay entre ";"
            User.UserType tipo = User.UserType.valueOf(datos[2]); //Guardo el tipo como lo tercero que hay entre ";"

            User user = new User(UID, alias, tipo); //Creo el usuario con los datos que guarde
            userByUID.put(UID, user); //También agrego el UID al Hash
        }
        br.close();
    }

    private void leerProcesos(String processPath) throws IOException {
        //Modifico el tutorial de archivos dado en clase de informática:

        BufferedReader br = Files.newBufferedReader(Path.of(processPath), StandardCharsets.UTF_8);
        String linea = br.readLine(); // leo encabezado: pid;uid;name;events

        while ((linea = br.readLine()) != null) {

            String[] datos = linea.split(";", 4); //El 4 aclara que cuando llegue a una cuarta columna (eventos) deje de dividir

            int PID = Integer.parseInt(datos[0]);
            int UID = Integer.parseInt(datos[1]);
            String nombre = datos[2];
            String eventosTexto = datos[3];

            User propietario = userByUID.get(UID); //Con el UID que conseguí obtengo el objeto User con ese UID

            MyList<Event> eventos = leerEventos(eventosTexto); //Hay que leer la columna de eventos también

            DoorProcess proceso = new DoorProcess(PID, nombre, propietario, eventos); //Creo el objeto DoorProcess
            new_processes.enqueue(proceso); //Agrego el proceso a la lista de procesos nuevos
            processesByPID.put(PID, proceso); //Agrego el PID a su Hash
        }
        br.close();
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

            for (int i = 0; i < instruccionesSeparadas.length; i++) {
                instrucciones.add(instruccionesSeparadas[i].trim()); //Cada instruccion que había dividido la agrego la lista de instrucciones
            }

            Event evento = new Event(tipo, instrucciones); //Creo el evento
            eventos.add(evento); //Lo agrego a la lista
        }
        return eventos;
    }


    @Override
    public void prepareProcesses() {
        //Aca vamos a tener que agarrar todos los procesos nuevos, de ahi calcular su prioridad y lueg0
        //mover al heap de estado pendiente

        
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void executeNextProcess() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessOk() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessError() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void terminateProcess(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatus() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByUser(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByProcess(int pid) {
        System.out.println("IMPLEMENTAR");
    }
}
