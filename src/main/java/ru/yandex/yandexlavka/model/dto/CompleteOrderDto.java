package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Data
@Validated
public class CompleteOrderDto {

    @JsonProperty("courier_id")
    private long courierId;

    @JsonProperty("order_id")
    private long orderId;

    @JsonProperty("complete_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime completeTime;

}
