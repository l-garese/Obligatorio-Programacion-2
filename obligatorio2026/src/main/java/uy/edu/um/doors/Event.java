package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class Event {
    private EventType tipo;
    private MyList<String> instrucciones;

    public enum EventType{
        CPU,
        RAM,
        DISK
    }

    public Event(EventType tipo, MyList<String> instrucciones){
        this.tipo = tipo;
        this.instrucciones = instrucciones;
    }

    public EventType getTipo() {
        return tipo;
    }

    public MyList<String> getInstrucciones() {
        return instrucciones;
    }
}
