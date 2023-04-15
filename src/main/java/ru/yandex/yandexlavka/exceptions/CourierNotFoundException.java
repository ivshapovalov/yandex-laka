package ru.yandex.yandexlavka.exceptions;

public class CourierNotFoundException extends BusinessException {
    public CourierNotFoundException(Long courierId) {
            super("Could not find courier with id " + courierId);
        }
}
