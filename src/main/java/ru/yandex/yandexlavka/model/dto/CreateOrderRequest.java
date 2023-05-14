package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @JsonProperty("orders")
    @NotNull
    List<@Valid CreateOrderDto> orders;

    public List<CreateOrderDto> getOrders() {
        return orders;
    }

}
