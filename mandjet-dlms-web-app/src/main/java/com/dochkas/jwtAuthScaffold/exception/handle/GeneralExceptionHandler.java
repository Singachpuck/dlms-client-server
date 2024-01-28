package com.dochkas.jwtAuthScaffold.exception.handle;

import com.dochkas.jwtAuthScaffold.exception.EntityException;
import com.dochkas.jwtAuthScaffold.exception.MandjetException;
import com.dochkas.jwtAuthScaffold.exception.MaxNumberExceededException;
import com.dochkas.jwtAuthScaffold.exception.NotOwnerException;
import com.dochkas.jwtAuthScaffold.model.message.DefaultErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({EntityException.class})
    ResponseEntity<Object> entityError(Exception exception, WebRequest request) {
        final DefaultErrorMessage message = new DefaultErrorMessage(HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage());
        return this.handleExceptionInternal(exception,
                message,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler({MaxNumberExceededException.class, NotOwnerException.class})
    ResponseEntity<Object> entityNumberError(Exception exception, WebRequest request) {
        final DefaultErrorMessage message = new DefaultErrorMessage(HttpStatus.FORBIDDEN.getReasonPhrase(), exception.getMessage());
        return this.handleExceptionInternal(exception,
                message,
                new HttpHeaders(),
                HttpStatus.FORBIDDEN,
                request);
    }

    @ExceptionHandler({MandjetException.class})
    ResponseEntity<Object> mandjetError(Exception exception, WebRequest request) {
        final DefaultErrorMessage message = new DefaultErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), exception.getMessage());
        return this.handleExceptionInternal(exception,
                message,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return this.entityError(new EntityException(ex.getAllErrors().get(0).getDefaultMessage()), request);
    }
}
