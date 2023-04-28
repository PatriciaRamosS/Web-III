package br.com.ada.apostaapi.exceptions;

public class FinishedGameException extends RuntimeException {
    public FinishedGameException(String message) {
        super(message);
    }
}
