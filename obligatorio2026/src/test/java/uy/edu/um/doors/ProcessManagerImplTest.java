package uy.edu.um.doors;

import org.junit.jupiter.api.Test;
import uy.edu.um.tad.heap.EmptyHeapException;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;

import static org.junit.jupiter.api.Assertions.*;

class ProcessManagerImplTest {

    @Test
    void loadProcessAndUserData() {
    }

    @Test
    void InsercionPorPrioridadALaQueue() throws Exception { //Pruebo la funcion prepareprocess
        ProcessManagerImpl manager=new ProcessManagerImpl();
        MyHeap<DoorProcess> procesospendientes=manager.getPendingProcesses();
        MyQueue<DoorProcess> procesosNuevos= manager.getNew_processes();
        MyList<Event> eventos1=new MyLinkedListImpl<>();
        MyList<Event> eventos2=new MyLinkedListImpl<>();
        MyList<Event> eventos3=new MyLinkedListImpl<>();

        eventos1.add(new Event(Event.EventType.CPU, new MyLinkedListImpl<>()));
        eventos1.add(new Event(Event.EventType.RAM, new MyLinkedListImpl<>()));
        eventos1.add(new Event(Event.EventType.DISK, new MyLinkedListImpl<>()));
        //Este evento tendra una prioridad de 100
        eventos2.add(new Event(Event.EventType.CPU, new MyLinkedListImpl<>()));
        eventos2.add(new Event(Event.EventType.RAM, new MyLinkedListImpl<>()));
        //Este evento tendra una prioridad de 69
        eventos3.add(new Event(Event.EventType.CPU, new MyLinkedListImpl<>()));
        User usuario1=new User(111,"Usuario1", User.UserType.ADMIN);
        User usuario2=new User(222,"Usuario2", User.UserType.ADMIN);
        User usuario3=new User(222,"Usuario3", User.UserType.GENERIC);

        DoorProcess proceso1=new DoorProcess(1,"Proceso1",usuario1,eventos1);
        DoorProcess proceso2=new DoorProcess(2,"Proceso2",usuario2,eventos2);
        DoorProcess proceso3=new DoorProcess(3,"Proceso3",usuario3,eventos3);
        procesosNuevos.enqueue(proceso1);
        procesosNuevos.enqueue(proceso2);
        manager.prepareProcesses(); //Aca pasa con nuestra funcion de nuevos a pendientes
        DoorProcess procesomayorprioridad=procesospendientes.remove();
        assertEquals(100,procesomayorprioridad.getPrioridad());
        //Aca chequeo si la prioridad del proceso que saque es igual a la del proceso que puse aca como mayor(proceso 1)
    }

    @Test
    void casoHeapVacio() {
        ProcessManagerImpl manager=new ProcessManagerImpl(); //Creo un manager nuevo
        assertThrows(EmptyHeapException.class, () -> {
            manager.executeNextProcess();
        }, "El manager debería tirar una excepción si el heap está vacío");
    }


    @Test
    void finishProcessOk() {
    }

    @Test
    void finishProcessError() {
    }

    @Test
    void terminateProcess() {
    }

    @Test
    void printStatus() {
    }

    @Test
    void printStatusVerbose() {
    }

    @Test
    void printStatusByUser() {
    }

    @Test
    void printStatusByProcess() {
    }
}