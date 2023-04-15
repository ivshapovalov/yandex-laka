package ru.yandex.yandexlavka.exceptions;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

@ControllerAdvice
@Log
class ExceptionHandlerAdvice {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Object> handleRequestNotPermitted(RequestNotPermitted ex, HttpServletRequest request) {
        log.warning(format("Request to path '%s' is blocked due to rate-limiting. %s",
                request.getRequestURI(), ex.getMessage()));
        return new ResponseEntity<>("Too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }

    @ResponseBody
    @ExceptionHandler(
            {CourierNotFoundException.class,
                    CourierOrderNotFoundException.class,
                    OrderNotFoundException.class,
                    OrderAlreadyCompletedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String businessExceptionHandler(BusinessException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public String handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        constraintViolations.forEach(violation ->
        {
            String[] path = violation.getPropertyPath().toString().split("\\.");
            errors.put(path[path.length - 1], violation.getMessage());
        });

        return errors;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Map<String, String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put(e.getName(), e.getMessage());
        return errors;
    }
}
