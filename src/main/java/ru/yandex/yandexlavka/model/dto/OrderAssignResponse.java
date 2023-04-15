package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderAssignResponse {

    @JsonProperty("date")
    @Valid
    private LocalDate date;

    @JsonProperty("orders")
    @Valid
    private List<CouriersGroupOrders> couriersGroupOrders;

}
