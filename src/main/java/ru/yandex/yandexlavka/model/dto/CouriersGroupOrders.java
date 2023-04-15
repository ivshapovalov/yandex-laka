package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import ru.yandex.yandexlavka.model.entity.GroupOrders;

@Data
public class CouriersGroupOrders {

    @JsonProperty("courier_id")
    private long courierId;

    @JsonProperty("orders")
    @Valid
    private GroupOrders groupOrders;

}
