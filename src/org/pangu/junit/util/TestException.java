package org.pangu.junit.util;

public class TestException extends Exception {

    public TestException(String message, Throwable t) {
        super(message,t);
    }
}
