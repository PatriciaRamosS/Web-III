package br.com.ada.usuarioapi.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class ExceptionHandlerControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DuplicatedUserException.class)
    public ResponseEntity<Object> handleDuplicatedUserExcepetion(DuplicatedUserException ex) {
        String errorMessage = ex.getMessage();
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(errorMessage, httpStatus);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        String errorMessage = ex.getMessage();
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(errorMessage, httpStatus);
    }

    @ExceptionHandler(UnauthorizedBalanceTransactionException.class)
    public ResponseEntity<Object> handleUnauthorizedBalanceTransactionException(UnauthorizedBalanceTransactionException ex) {
        String errorMessage = ex.getMessage();
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(errorMessage, httpStatus);
    }




}
