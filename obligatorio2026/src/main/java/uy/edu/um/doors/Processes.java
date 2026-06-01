package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class Processes implements Comparable<Processes> {
    private int PID;
    private String nombre;
    private User propietario;
    private int prioridad;
    private ProcessState estado;
    private MyList<Event> eventosAsociados;

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

    public Processes(int PID, String nombre, User propietario, MyList<Event> eventosAsociados){
        this.PID = PID;
        this.nombre = nombre;
        this.propietario = propietario;
        this.prioridad = 0;
        this.estado = ProcessState.NEW;
        this.eventosAsociados = eventosAsociados;
    }

    //Getters

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
}
