package br.com.ada.jogosapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Object> handleGameNotFoundException(GameNotFoundException ex) {
        String errorMessage = ex.getMessage();
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(errorMessage, httpStatus);
    }

    @ExceptionHandler(UnavaliableGameException.class)
    public ResponseEntity<Object> handleUnavailableGameException(UnavaliableGameException ex) {
        String errorMessage = ex.getMessage();
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(errorMessage, httpStatus);
    }
}
