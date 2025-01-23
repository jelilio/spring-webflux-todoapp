package io.github.jelilio.todoapp.exception.handler;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.jelilio.todoapp.exception.AlreadyExistException;
import io.github.jelilio.todoapp.exception.AuthenticationException;
import io.github.jelilio.todoapp.exception.NotFoundException;
import io.github.jelilio.todoapp.exception.model.ErrorDetail;

import java.time.Instant;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandlers {
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorDetail> handleNotFoundException(NotFoundException ex) {
    return ResponseEntity.status(NOT_FOUND).body(
        new ErrorDetail(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase(), ex.getMessage(), Instant.now())
    );
  }

  @ExceptionHandler(AlreadyExistException.class)
  public ResponseEntity<ErrorDetail> handleAlreadyExistException(AlreadyExistException ex) {
    return ResponseEntity.status(BAD_REQUEST).body(
        new ErrorDetail(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase(), ex.getMessage(), Instant.now())
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorDetail> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity.status(UNAUTHORIZED).body(
        new ErrorDetail(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), Instant.now())
    );
  }
}
