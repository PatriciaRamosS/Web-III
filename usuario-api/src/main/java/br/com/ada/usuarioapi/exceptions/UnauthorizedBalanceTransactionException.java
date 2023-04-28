package br.com.ada.usuarioapi.exceptions;

public class UnauthorizedBalanceTransactionException extends RuntimeException{
    public UnauthorizedBalanceTransactionException(String message) {
        super(message);
    }
}
