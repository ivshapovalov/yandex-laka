package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CompleteOrderRequest {
    @Valid
    @JsonProperty("complete_info")
    @NotEmpty
    List<@Valid CompleteOrderDto> completeOrders;

}
