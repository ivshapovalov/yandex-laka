package ru.yandex.yandexlavka.exceptions;

import static java.lang.String.format;

public class CourierOrderNotFoundException extends BusinessException {
    public CourierOrderNotFoundException(long courierId, long orderId) {
        super(format("Could not find courier's %d order %d", courierId, orderId));
    }
}
