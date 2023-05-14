package ru.yandex.yandexlavka.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.yandexlavka.utils.Utils;

public class TimeWindowValidator implements
        ConstraintValidator<TimeWindowConstraint, String> {

    @Override
    public void initialize(TimeWindowConstraint timeWindow) {
    }

    @Override
    public boolean isValid(String interval,
                           ConstraintValidatorContext cxt) {
        if (interval != null) {
            try {
                Utils.convertStringIntervalToSecondsInterval(interval);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }
}
