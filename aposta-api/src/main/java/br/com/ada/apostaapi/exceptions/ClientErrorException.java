package br.com.ada.apostaapi.exceptions;

import org.springframework.web.reactive.function.server.ServerRequest;

public class ClientErrorException extends RuntimeException{
    public ClientErrorException(String message){
        super(message);
    }
}
