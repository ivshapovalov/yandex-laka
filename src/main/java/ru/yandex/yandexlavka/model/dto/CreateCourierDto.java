package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import ru.yandex.yandexlavka.config.validation.FirstOrder;
import ru.yandex.yandexlavka.config.validation.SecondOrder;
import ru.yandex.yandexlavka.config.validation.TimeWindowConstraint;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.Region;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@GroupSequence({CreateCourierDto.class, FirstOrder.class, SecondOrder.class})
public class CreateCourierDto {

    @JsonProperty("courier_type")
    @Valid
    private CourierDto.CourierTypeEnum courierType;

    @JsonProperty("regions")
    @NotEmpty
    private Set<@Valid @Range(min = 1, max = Integer.MAX_VALUE, groups = FirstOrder.class) Integer> regions =
            new HashSet<>();

    @JsonProperty("working_hours")
    @NotEmpty
    private Set<
            @Valid
            @Pattern(regexp = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]",
                    groups = FirstOrder.class)
            @TimeWindowConstraint(groups = SecondOrder.class)
                    String> workingHours = new HashSet<>();

    public CourierDto toCourierDto(List<Region> regions) {
        CourierDto courierDto = new CourierDto();
        courierDto.setCourierType(this.getCourierType());
        courierDto.setWorkingHours(this.getWorkingHours().stream().sorted().collect(Collectors.toList()));
        courierDto.setRegions(regions.stream().sorted(Comparator.comparingInt(Region::getId)).collect(Collectors.toList()));
        return courierDto;
    }

}
