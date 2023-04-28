package br.com.ada.jogosapi.exceptions;

public class UnavaliableGameException extends RuntimeException{
    public UnavaliableGameException(String message){
        super(message);
    }
}
