package ru.yandex.yandexlavka.exceptions;

import static java.lang.String.format;

public class OrderAlreadyCompletedException extends BusinessException {
    public OrderAlreadyCompletedException(Long orderId) {
        super(format("Order %d already completed", orderId));
    }
}
