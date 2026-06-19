package exceptions;

public class NoRunningProcessException extends Exception {
    public NoRunningProcessException(String mensaje) {
        super(mensaje);
    }
}