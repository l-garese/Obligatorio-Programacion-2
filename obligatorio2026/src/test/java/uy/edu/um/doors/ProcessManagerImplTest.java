package uy.edu.um.doors;

import exceptions.NoRunningProcessException;
import exceptions.UsuarioNoEncontradoException;
import org.junit.jupiter.api.Test;
import uy.edu.um.tad.heap.EmptyHeapException;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.MyStack;

import static org.junit.jupiter.api.Assertions.*;

class ProcessManagerImplTest {


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
    void finishProcessOk() throws Exception {
        ProcessManagerImpl manager=new ProcessManagerImpl();
        MyHeap<DoorProcess> procesospendientes=manager.getPendingProcesses();
        MyQueue<DoorProcess> procesosNuevos= manager.getNew_processes();
        MyStack<DoorProcess> procesosFinalizados = manager.getFinished_processes();
        MyList<Event> eventos1=new MyLinkedListImpl<>();
        User usuario1=new User(111,"Usuario1", User.UserType.ADMIN);
        DoorProcess proceso1=new DoorProcess(1,"Proceso1",usuario1,eventos1);
        procesosNuevos.enqueue(proceso1);
        manager.prepareProcesses();
        manager.executeNextProcess();
        manager.finishProcessOk();
        DoorProcess procesoFinalizado =procesosFinalizados.peek();
        assertEquals(DoorProcess.FinishedState.OK, procesoFinalizado.getfinishedState());
        assertEquals(DoorProcess.ProcessState.FINISHED, procesoFinalizado.getProcessState());
    }

    @Test
    void finishProcessError() throws Exception {
        ProcessManagerImpl manager=new ProcessManagerImpl();
        MyQueue<DoorProcess> procesosNuevos= manager.getNew_processes();
        MyStack<DoorProcess> procesosFinalizados = manager.getFinished_processes();
        MyList<Event> eventos1=new MyLinkedListImpl<>();
        User usuario1=new User(111,"Usuario1", User.UserType.ADMIN);
        DoorProcess proceso1=new DoorProcess(1,"Proceso1",usuario1,eventos1);
        procesosNuevos.enqueue(proceso1);
        manager.prepareProcesses();
        manager.executeNextProcess();
        manager.finishProcessError();
        DoorProcess procesoFinalizado =procesosFinalizados.peek();
        assertEquals(DoorProcess.FinishedState.ERROR, procesoFinalizado.getfinishedState());
        assertEquals(DoorProcess.ProcessState.FINISHED, procesoFinalizado.getProcessState());
    }

    @Test
    void terminateProcess() throws Exception {
        ProcessManagerImpl manager = new ProcessManagerImpl();
        MyQueue<DoorProcess> procesosNuevos = manager.getNew_processes();
        MyStack<DoorProcess> procesosFinalizados = manager.getFinished_processes();
        MyList<Event> eventos1 = new MyLinkedListImpl<>();
        User usuario1 = new User(111, "Usuario1", User.UserType.ADMIN);
        // Registrar el usuario en el hash del manager
        manager.getUserByUID().put(111, usuario1);
        DoorProcess proceso1 = new DoorProcess(1, "Proceso1", usuario1, eventos1);
        procesosNuevos.enqueue(proceso1);
        manager.prepareProcesses();
        manager.executeNextProcess();
        manager.terminateProcess(111);
        DoorProcess procesoFinalizado = procesosFinalizados.peek();
        assertEquals(DoorProcess.FinishedState.TERMINATED, procesoFinalizado.getfinishedState());
        assertEquals(DoorProcess.ProcessState.FINISHED, procesoFinalizado.getProcessState());
        assertEquals(usuario1, procesoFinalizado.getTerminadoPor());
    }

    @Test
    void terminateProcessUsuarioNulo() throws Exception{
        ProcessManagerImpl manager=new ProcessManagerImpl();
        MyQueue<DoorProcess> procesosNuevos= manager.getNew_processes();
        MyList<Event> eventos1=new MyLinkedListImpl<>();
        User usuario1 = new User(111, "Usuario1", User.UserType.ADMIN);
        DoorProcess proceso1=new DoorProcess(1,"Proceso1",usuario1,eventos1);
        procesosNuevos.enqueue(proceso1);
        manager.prepareProcesses();
        manager.executeNextProcess();
        assertThrows(UsuarioNoEncontradoException.class, () -> manager.terminateProcess(999));
    }

    @Test
    void StackLleno() throws Exception {
        ProcessManagerImpl manager = new ProcessManagerImpl();
        MyQueue<DoorProcess> procesosNuevos = manager.getNew_processes();
        MyStack<DoorProcess> procesosFinalizados = manager.getFinished_processes();
        MyList<Event> eventos1 = new MyLinkedListImpl<>();
        User usuario1 = new User(111, "Usuario1", User.UserType.ADMIN);

        DoorProcess proceso1 = new DoorProcess(1, "Proceso1", usuario1, eventos1);
        DoorProcess proceso2 = new DoorProcess(2, "Proceso2", usuario1, eventos1);
        DoorProcess proceso3 = new DoorProcess(3, "Proceso3", usuario1, eventos1);
        DoorProcess proceso4 = new DoorProcess(4, "Proceso4", usuario1, eventos1);
        procesosNuevos.enqueue(proceso1);
        procesosNuevos.enqueue(proceso2);
        procesosNuevos.enqueue(proceso3);
        manager.prepareProcesses();
        manager.executeNextProcess();
        manager.finishProcessOk();
        manager.executeNextProcess();
        manager.finishProcessOk();
        manager.executeNextProcess();
        manager.finishProcessOk();
        // El stack ahora está lleno (3 procesos)
        assertEquals(3, procesosFinalizados.size());
        procesosNuevos.enqueue(proceso4); //overflow
        manager.prepareProcesses();
        manager.executeNextProcess();
        manager.finishProcessOk();
        // Después del overflow el stack se vació y tiene solo el nuevo proceso
        assertEquals(1, procesosFinalizados.size());
        assertEquals(proceso4, procesosFinalizados.peek());
    }

    @Test
    void finishProcessErrorSinRunning() {
        ProcessManagerImpl manager = new ProcessManagerImpl();
        assertThrows(NoRunningProcessException.class, () -> manager.finishProcessError());
    }

    @Test
    void terminateProcessSinRunning() {
        ProcessManagerImpl manager = new ProcessManagerImpl();
        manager.getUserByUID().put(111, new User(111, "Usuario1", User.UserType.ADMIN));
        assertThrows(NoRunningProcessException.class, () -> manager.terminateProcess(111));
    }

    //tests de los prints
    private ProcessManagerImpl setupConDatos() {
        ProcessManagerImpl manager = new ProcessManagerImpl();

        User adminUser = new User(1, "usuarioAdmin", User.UserType.ADMIN);
        User genericUser = new User(2, "usuarioGenerico", User.UserType.GENERIC);
        manager.getUserByUID().put(1, adminUser);
        manager.getUserByUID().put(2, genericUser);

        // Proceso en ejecución
        DoorProcess running = new DoorProcess(100, "procesoCorriendo", adminUser, new MyLinkedListImpl<>());
        running.setPrioridad(900);
        manager.getPendingProcesses().insert(running);
        manager.executeNextProcess();

        // Pendientes
        DoorProcess pending1 = new DoorProcess(101, "procesoPendiente1", genericUser, new MyLinkedListImpl<>());
        pending1.setPrioridad(300);
        manager.getPendingProcesses().insert(pending1);

        DoorProcess pending2 = new DoorProcess(102, "ProcesoPendiente2", adminUser, new MyLinkedListImpl<>());
        pending2.setPrioridad(700);
        manager.getPendingProcesses().insert(pending2);

        // Finalizados
        DoorProcess finished1 = new DoorProcess(103, "procesoFinalizado1", genericUser, new MyLinkedListImpl<>());
        finished1.setEstado(DoorProcess.ProcessState.FINISHED);
        finished1.setFinishedState(DoorProcess.FinishedState.OK);
        manager.getFinished_processes().push(finished1);
        DoorProcess finished2 = new DoorProcess(104, "procesoFinalizado2", adminUser, new MyLinkedListImpl<>());
        finished2.setEstado(DoorProcess.ProcessState.FINISHED);
        finished2.setFinishedState(DoorProcess.FinishedState.ERROR);
        manager.getFinished_processes().push(finished2);

        return manager;
    }

    @Test
    void printStatus_noModificaEstructuras() {
        ProcessManagerImpl manager = setupConDatos();

        int pendingSizeAntes = manager.getPendingProcesses().size();
        int finishedSizeAntes = manager.getFinished_processes().size();
        DoorProcess runningAntes = manager.getRunningProcess();
        assertDoesNotThrow(manager::printStatus);
        assertEquals(pendingSizeAntes, manager.getPendingProcesses().size());
        assertEquals(finishedSizeAntes, manager.getFinished_processes().size());
        assertEquals(runningAntes, manager.getRunningProcess());
    }

    @Test
    void printStatusVerbose_noModificaEstructuras() {
        ProcessManagerImpl manager = setupConDatos();

        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("instruccion1");
        instrucciones.add("instruccion2");
        MyList<Event> eventos = new MyLinkedListImpl<>();
        eventos.add(new Event(Event.EventType.CPU, instrucciones));
        User genericUser = (User) manager.getUserByUID().get(2);
        DoorProcess conEventos = new DoorProcess(105, "proceso", genericUser, eventos);
        conEventos.setPrioridad(400);
        manager.getPendingProcesses().insert(conEventos);
        int pendingSizeAntes = manager.getPendingProcesses().size();
        int finishedSizeAntes = manager.getFinished_processes().size();

        assertDoesNotThrow(() -> manager.printStatusVerbose());
        assertEquals(pendingSizeAntes, manager.getPendingProcesses().size());
        assertEquals(finishedSizeAntes, manager.getFinished_processes().size());
    }

    @Test
    void printStatusByUser_noModificaEstructuras() {
        ProcessManagerImpl manager = setupConDatos();
        int pendingSizeAntes = manager.getPendingProcesses().size();
        int finishedSizeAntes = manager.getFinished_processes().size();

        assertDoesNotThrow(() -> manager.printStatusByUser(1));
        assertEquals(pendingSizeAntes, manager.getPendingProcesses().size());
        assertEquals(finishedSizeAntes, manager.getFinished_processes().size());
    }

    @Test
    void printStatusByUser_conUidInexistente_noRompe() {
        ProcessManagerImpl manager = setupConDatos();
        assertDoesNotThrow(() -> manager.printStatusByUser(9999));
    }

    @Test
    void printStatusByProcess_conPidExistente_noRompe() {
        ProcessManagerImpl manager = setupConDatos();
        assertDoesNotThrow(() -> manager.printStatusByProcess(101));
    }

    @Test
    void printStatusByProcess_conPidInexistente_noRompe() {
        ProcessManagerImpl manager = setupConDatos();
        assertDoesNotThrow(() -> manager.printStatusByProcess(9999));
    }

    @Test
    void printStatusByProcess_noModificaElHeap() {
        ProcessManagerImpl manager = setupConDatos();
        int sizeAntes = manager.getPendingProcesses().size();
        manager.printStatusByProcess(101);

        assertEquals(sizeAntes, manager.getPendingProcesses().size());
    }
}