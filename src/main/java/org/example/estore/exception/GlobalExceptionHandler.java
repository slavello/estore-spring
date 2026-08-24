package org.example.estore.exception;

import org.example.estore.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiError> outOfStock(OutOfStockException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiError> validation(BindException ex) {
        String msg = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::fieldMessage)
            .collect(Collectors.joining("; "));
        return ResponseEntity
            .badRequest()
            .body(new ApiError(msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ApiError("Некорректный формат тела запроса"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ApiError("Некорректное значение параметра: " + ex.getName()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ApiError("Операция нарушает целостность данных (запись используется либо дублируется)"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError("Внутренняя ошибка сервера: " + ex.getMessage()));
    }

    private String fieldMessage(FieldError f) {
        return f.getField() + ": " + f.getDefaultMessage();
    }
}
