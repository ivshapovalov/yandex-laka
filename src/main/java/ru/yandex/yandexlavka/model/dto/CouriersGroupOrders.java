package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import ru.yandex.yandexlavka.model.entity.GroupOrders;

import java.util.List;

@Data
public class CouriersGroupOrders {

    @JsonProperty("courier_id")
    private final long courierId;

    @JsonProperty("orders")
    @Valid
    private final List<GroupOrders> groupOrders;

}
