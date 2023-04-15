package ru.yandex.yandexlavka.exceptions;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long orderId) {
            super("Could not find order with id " + orderId);
        }
}
