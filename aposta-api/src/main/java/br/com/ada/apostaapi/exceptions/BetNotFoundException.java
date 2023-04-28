package br.com.ada.apostaapi.exceptions;

public class BetNotFoundException extends RuntimeException{
    public BetNotFoundException(String message){
        super((message));
    }
}
