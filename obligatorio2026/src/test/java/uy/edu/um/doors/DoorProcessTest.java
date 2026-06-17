package uy.edu.um.doors;

import org.junit.jupiter.api.Test;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;

import static org.junit.jupiter.api.Assertions.*;

class DoorProcessTest {

    @Test
    void calcularPrioridadParaAdmin() throws Exception {
        // 1. ARRANGE
        //Preparo todo para calcular

        // Creamos la lista de eventos para el proceso
        MyList<Event> listaEventos = new MyLinkedListImpl<>();

        // Creamos eventos y los añadimos a la lista de eventos del proceso, la lista de instrucciones es vacia
        listaEventos.add(new Event(Event.EventType.CPU, new MyLinkedListImpl<>()));
        listaEventos.add(new Event(Event.EventType.RAM, new MyLinkedListImpl<>()));
        listaEventos.add(new Event(Event.EventType.DISK, new MyLinkedListImpl<>()));
        User admin = new User(123, "alias", User.UserType.ADMIN);
        DoorProcess proceso = new DoorProcess(123, "ProcesoTest", admin, listaEventos);

        int prioridad_resultante = proceso.calcularPrioridad();
        assertEquals(100, prioridad_resultante);
        //El  100 lo calculamos a mano y queremos que la prioridad que nosotros calculamos nos de lo mismo
    }
    @Test
    void calcularPrioridadParaGeneric() throws Exception {
        // 1. ARRANGE
        //Preparo todo para calcular

        // Creamos la lista de eventos para el proceso
        MyList<Event> listaEventos = new MyLinkedListImpl<>();

        // Creamos eventos y los añadimos a la lista de eventos del proceso, la lista de instrucciones es vacia
        listaEventos.add(new Event(Event.EventType.CPU, new MyLinkedListImpl<>()));
        listaEventos.add(new Event(Event.EventType.RAM, new MyLinkedListImpl<>()));
        listaEventos.add(new Event(Event.EventType.DISK, new MyLinkedListImpl<>()));
        User generic = new User(123, "alias2", User.UserType.GENERIC);
        DoorProcess proceso = new DoorProcess(123, "ProcesoTest", generic, listaEventos);

        int prioridad_resultante = proceso.calcularPrioridad();
        assertEquals(52, prioridad_resultante);
        //El  52 lo calculamos a mano y queremos que la prioridad que nosotros calculamos nos de lo mismo

    }
    @Test
    public void eventosNulos() throws Exception {
        MyList<Event> eventosVacios= new MyLinkedListImpl<>();
        //No le voy a agregar ningun evento por lo tanto el divisor sera 0
        User usuario=new User(111,"Usuario1", User.UserType.GENERIC);
        DoorProcess proceso=new DoorProcess(111,"Procesotest2",usuario,eventosVacios);
        int prioridadResultante=proceso.calcularPrioridad();
        assertEquals(0,prioridadResultante,"Si un proceso no tiene eventos la priordiad debe ser 0");

    }

}