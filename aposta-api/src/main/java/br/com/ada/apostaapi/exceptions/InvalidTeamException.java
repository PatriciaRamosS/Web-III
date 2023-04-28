package br.com.ada.apostaapi.exceptions;

public class InvalidTeamException extends RuntimeException {
    public InvalidTeamException(String message) {
        super(message);
    }
}
