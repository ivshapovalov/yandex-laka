package ru.yandex.yandexlavka.exceptions;

import static java.lang.String.format;

public class OrderImpossibleException extends BusinessException {
    public OrderImpossibleException(Long orderId) {
        super(format("Order %d impossible!", orderId));
    }
}
