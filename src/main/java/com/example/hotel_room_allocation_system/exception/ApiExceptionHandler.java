package com.example.hotel_room_allocation_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final URI TYPE_VALIDATION = URI.create("urn:problem:validation-error");
    private static final URI TYPE_BAD_REQUEST = URI.create("urn:problem:bad-request");
    private static final URI TYPE_IDEMPOTENCY_CONFLICT = URI.create("urn:problem:idempotency-conflict");
    private static final URI TYPE_INTERNAL = URI.create("urn:problem:internal-server-error");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleVlidation(MethodArgumentNotValidException ex, HttpServletRequest request){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(TYPE_VALIDATION);
        pd.setTitle("Validation failed");
        pd.setDetail("Request validation failed");
        pd.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, Object>> errors = new ArrayList<>();
        for(FieldError fe : ex.getBindingResult().getFieldErrors()){
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("filed", fe.getField());
            err.put("message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"));
            err.put("rehectedValue", fe.getRejectedValue());
            errors.add(err);
        }
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(TYPE_BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(TYPE_BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return pd;
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail handleIdempotencyConflict(IdempotencyConflictException ex, HttpServletRequest request){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(TYPE_IDEMPOTENCY_CONFLICT);
        pd.setTitle("Idempotency conflict");
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return pd;
    }

    @ExceptionHandler(ErrorResponseException.class)
    ProblemDetail handleErrorResponse(ErrorResponseException ex, HttpServletRequest request){
        ProblemDetail pd = ex.getBody();
        if(pd.getType() == null || pd.getType().equals(URI.create("about:blank"))){
            pd.setType(TYPE_BAD_REQUEST);
        }
        if(pd.getInstance() == null){
            pd.setInstance(URI.create(request.getRequestURI()));
        }

        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(Exception ex, HttpServletRequest request){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(TYPE_INTERNAL);
        pd.setTitle("internal server error");
        pd.setDetail("An unexpected error occurred.");
        pd.setInstance(URI.create(request.getRequestURI()));

        return pd;
    }

}
