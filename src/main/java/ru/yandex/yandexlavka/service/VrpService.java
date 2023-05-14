package ru.yandex.yandexlavka.service;

import jakarta.validation.constraints.NotEmpty;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.OrderDto;

import java.time.LocalDate;
import java.util.List;

public interface VrpService {

    OrderAssignResponse solve(
            LocalDate currentDate,
            @NotEmpty List<CourierDto> couriers,
            @NotEmpty List<OrderDto> orders);

}
