package org.example.estore.exception;

/**
 * Товар отсутствует в наличии в выбранном магазине.
 */
public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }
}
