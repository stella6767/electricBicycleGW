package net.linalabs.station.handler.customexception;

public class TimeOutException extends RuntimeException {

    public TimeOutException(String message) {
        super(message);
    }
}
