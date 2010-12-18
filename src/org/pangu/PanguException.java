package org.pangu;

public class PanguException extends Exception {

    private static final long serialVersionUID = 1L;

    public PanguException(String message) {
        super(message);
    }
    
    public PanguException(String message, Throwable t) {
        super(message,t);
    }
}
