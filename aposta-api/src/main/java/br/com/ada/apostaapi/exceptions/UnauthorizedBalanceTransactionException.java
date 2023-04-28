package br.com.ada.apostaapi.exceptions;

public class UnauthorizedBalanceTransactionException extends RuntimeException{
    public UnauthorizedBalanceTransactionException(String message) {
        super(message);
    }
}
