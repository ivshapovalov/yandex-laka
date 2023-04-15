package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Entity
@Table(name = "regions")
@Data
public class Region {

    @Id
    @NotNull
    @Range(min = 1, max = Integer.MAX_VALUE)
    @JsonProperty("id")
    @Column(name = "id")
    private int id;

    public Region() {

    }

    public Region(int id) {
        this.id = id;
    }
}
