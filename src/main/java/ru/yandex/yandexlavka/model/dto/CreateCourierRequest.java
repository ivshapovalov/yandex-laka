package ru.yandex.yandexlavka.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCourierRequest {
    @JsonProperty("couriers")
    @NotNull
    List<@Valid CreateCourierDto> couriers;

}
