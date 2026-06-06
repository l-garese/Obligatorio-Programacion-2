package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class DoorProcess implements Comparable<DoorProcess>{
    private int PID;
    private String nombre;
    private User propietario;
    private int prioridad;
    private ProcessState estado;
    private MyList<Event> eventosAsociados;
    private User terminadoPor; //se registra cuando un usuario fuerza que el proceso termine
    private static final int MAX_FINISHED = 3;
    private FinishedState finishedState;// use un valor cualquiera porque en la letra dice que es una constante definida por el sistema


    @Override
    public int compareTo(DoorProcess o) {
        return Integer.compare(this.prioridad, o.prioridad); // compara floats, devuelve int

        //lo va a usar el heap de pendientes, que compara por prioridad
    }

    public String getfinishedState() {
        return finishedState.toString();
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


    public FinishedState getFinishedState() {
        return finishedState;
    }

    public static int getMaxFinished() {
        return MAX_FINISHED;
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

    public float getPrioridad() {
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
        int contadroRAM=0;
        int contadorCPU=0;
        int contadorDisco=0;
        for(int i=0;i<eventosAsociados.size();i++){
        Event evento=eventosAsociados.get(i);
        switch (evento.getTipo()){
            case CPU:
                contadorCPU ++;
                break;
            case RAM :
                contadroRAM++;
                break;
            case DISK :
                contadorDisco++;
                break;
         }
        }
        int pevents=contadroRAM+contadorCPU+contadorDisco; //Sumamos en vez de size porque asi no recorremos de nuevo la lista

        switch (propietario.getTipo()){
        case ADMIN :{
            return (int) (((8*contadorCPU + 2*contadroRAM+2*contadorDisco)/((float)pevents))+32*(pevents));

        }
        case GENERIC:{
            return (int) (((8*contadorCPU + 2*contadroRAM+2*contadorDisco)/((float)pevents))+16*(pevents));

            }
        }
    //dividimos por caso porque cambia el numero W
        return 0; //Caso que no matchee nada


    }

}
