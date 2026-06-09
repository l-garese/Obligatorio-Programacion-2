package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.list.Node;

public class DoorProcess implements Comparable<DoorProcess>{
    private final int PID;
    private final String nombre;
    private final User propietario;
    private int prioridad;
    private ProcessState estado;
    private final MyList<Event> eventosAsociados;
    private User terminadoPor; //se registra cuando un usuario fuerza que el proceso termine
    private FinishedState finishedState;// use un valor cualquiera porque en la letra dice que es una constante definida por el sistema

    @Override
    public int compareTo(DoorProcess o) {
        return Integer.compare(this.prioridad, o.prioridad);
        //lo va a usar el heap de pendientes, que compara por prioridad
    }

    public enum ProcessState{
        NEW,
        PENDING,
        RUNNING, //Solo puede haber uno
        FINISHED //Hay de tres tipos, especificado en FinishedState
    }

    public enum FinishedState{
        OK,
        ERROR,
        TERMINATED
    }

    public DoorProcess(int PID, String nombre, User propietario, MyList<Event> eventosAsociados) {
        this.PID = PID;
        this.nombre = nombre;
        this.propietario = propietario;
        this.prioridad = 0;
        this.estado = ProcessState.NEW;
        this.eventosAsociados = eventosAsociados;
        this.terminadoPor = null;
    }

    //Getters

    public FinishedState getfinishedState() {
        return finishedState;
    }

    public User getTerminadoPor() {
        return terminadoPor;
    }

    public int getPID() {
        return PID;
    }

    public String getNombre() {
        return nombre;
    }

    public User getPropietario() {
        return propietario;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public ProcessState getEstado() {
        return estado;
    }

    public MyList<Event> getEventosAsociados() {
        return eventosAsociados;
    }

    //Setters

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public void setEstado(ProcessState estado) {
        this.estado = estado;
    }

    public void setTerminadoPor(User terminadoPor) {
        this.terminadoPor = terminadoPor;
    }

    public void setFinishedState(FinishedState finishedState) {
        this.finishedState = finishedState;
    }

    public int calcularPrioridad() throws Exception {
        int contadorRAM = 0;
        int contadorCPU = 0;
        int contadorDisco = 0;
        Node<Event> nodo = eventosAsociados.getFirst();
        while (nodo != null) {
            Event evento = nodo.getValue();
            switch (evento.getTipo()) {
                case CPU:
                    contadorCPU++;
                    break;
                case RAM:
                    contadorRAM++;
                    break;
                case DISK:
                    contadorDisco++;
                    break;
            }
            nodo = nodo.getNext();
        }
        int events = contadorRAM + contadorCPU + contadorDisco; //Sumamos en vez de size porque asi no recorremos de nuevo la lista
        switch (propietario.getTipo()){
            case ADMIN :{
                return (int) (((8*contadorCPU + 2*contadorRAM+2*contadorDisco)/((float)events))+32*(events));
            }
            case GENERIC:{
                return (int) (((8*contadorCPU + 2*contadorRAM+2*contadorDisco)/((float)events))+16*(events));
            }
        }
        //dividimos por caso porque cambia el número W
        return 0; //Caso que no hizo match
    }

}
