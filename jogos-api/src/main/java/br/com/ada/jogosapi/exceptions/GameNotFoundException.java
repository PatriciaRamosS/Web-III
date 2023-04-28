package br.com.ada.jogosapi.exceptions;

public class GameNotFoundException extends RuntimeException{
    public GameNotFoundException(String message){
        super(message);
    }
}
