package com.example.demo.handlers;

import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.ExceptionHandlerResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> details = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        var body = new ExceptionHandlerResponseDTO(
                "Constraint violation",
                status.getReasonPhrase(),
                status.value(),
                ex.getClass().getSimpleName(),
                details,
                request.getDescription(false)
        );

        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = new ArrayList<>();
        boolean ageMessageAdded = false;
        for (ObjectError err : ex.getBindingResult().getAllErrors()) {
            if (err instanceof FieldError fe) {
                if ("age".equals(fe.getField())) {
                    if (!ageMessageAdded) {
                        details.add("Selecteaza o varsta intre 18 si 100 de ani");
                        ageMessageAdded = true;
                    }
                    continue; // skip default age message
                }
                details.add(fe.getField() + ": " + fe.getDefaultMessage());
            } else {
                details.add(err.getObjectName() + ": " + err.getDefaultMessage());
            }
        }
        if (details.isEmpty() && !ageMessageAdded) {
            details.add("Selecteaza o varsta intre 18 si 100 de ani");
            ageMessageAdded = true;
        }

        HttpStatus httpStatus = HttpStatus.valueOf(status.value());

        // If only age validation failed, return simple map so frontend picks 'message'
        if (ageMessageAdded && details.size() == 1) {
            return ResponseEntity.status(httpStatus).body(java.util.Map.of("message", details.get(0)));
        }

        var body = new ExceptionHandlerResponseDTO(
                "Validation failed",
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                MethodArgumentNotValidException.class.getSimpleName(),
                details,
                request.getDescription(false)
        );

        return handleExceptionInternal(ex, body, headers, httpStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        var body = new ExceptionHandlerResponseDTO(
                "Malformed JSON request",
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                HttpMessageNotReadableException.class.getSimpleName(),
                List.of(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, headers, httpStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        var body = new ExceptionHandlerResponseDTO(
                "Missing request parameter",
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                MissingServletRequestParameterException.class.getSimpleName(),
                List.of(ex.getParameterName() + " is required"),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, headers, httpStatus, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String param = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        var body = new ExceptionHandlerResponseDTO(
                "Type mismatch",
                status.getReasonPhrase(),
                status.value(),
                MethodArgumentTypeMismatchException.class.getSimpleName(),
                List.of(param + " must be of type " + required),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        var body = new ExceptionHandlerResponseDTO(
                "Data integrity violation",
                status.getReasonPhrase(),
                status.value(),
                DataIntegrityViolationException.class.getSimpleName(),
                List.of(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<Object> handleCustomExceptions(CustomException ex, WebRequest request) {
        var body = new ExceptionHandlerResponseDTO(
                ex.getResource(),
                ex.getStatus().getReasonPhrase(),
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getValidationErrors(),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), ex.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        var body = new ExceptionHandlerResponseDTO(
                "No handler found",
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                NoHandlerFoundException.class.getSimpleName(),
                List.of(ex.getHttpMethod() + " " + ex.getRequestURL()),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, headers, httpStatus, request);
    }
}
