package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.OrderColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CreateOrderDto {

    @Valid
    @JsonProperty("weight")
    private float weight;

    @Valid
    @JsonProperty("region")
    @Range(min = 1,max=Integer.MAX_VALUE)
    private int region;

    @Valid
    @JsonProperty("delivery_hours")
    @NotEmpty
    private
    Set<@Valid @Pattern(regexp = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]") String>
            deliveryHours = new LinkedHashSet<>();

    @Valid
    @JsonProperty("cost")
    private int cost;

    public OrderDto toOrderDto(Region region) {
        OrderDto orderDto = new OrderDto();
        orderDto.setCost(this.getCost());
        orderDto.setRegion(region);
        orderDto.setWeight(this.getWeight());
        orderDto.setDeliveryHours(this.getDeliveryHours().stream().sorted().collect(Collectors.toList()));
        return orderDto;
    }
}
