package Exceptions;

public class EmptyConfigFieldException extends Exception {
    public EmptyConfigFieldException(String message) {
        super(message);
    }
}