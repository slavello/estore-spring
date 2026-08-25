package org.example.estore.exception;

/** Запись не найдена */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
