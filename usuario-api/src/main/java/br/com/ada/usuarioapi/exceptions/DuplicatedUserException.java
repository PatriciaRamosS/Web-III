package br.com.ada.usuarioapi.exceptions;

public class DuplicatedUserException extends RuntimeException{
    public DuplicatedUserException(String message){
        super(message);
    }
}
