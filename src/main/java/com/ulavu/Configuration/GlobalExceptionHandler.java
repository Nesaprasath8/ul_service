package com.ulavu.Configuration;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ulavu.Entity.UL_Response;

/**
 * Central place for turning validation failures (and any other unhandled
 * exception) into the project's standard UL_Response shape, instead of each
 * controller deciding ad hoc how much of an exception's internals to expose.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UL_Response> handleValidation(MethodArgumentNotValidException ex) {
        UL_Response response = new UL_Response();
        response.status = "error";
        response.result = null;
        response.message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UL_Response> handleUnexpected(Exception ex) {
        // Full detail goes to the server log only - never to the client.
        log.error("Unhandled exception", ex);
        UL_Response response = new UL_Response();
        response.status = "error";
        response.result = null;
        response.message = "Internal server error";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
