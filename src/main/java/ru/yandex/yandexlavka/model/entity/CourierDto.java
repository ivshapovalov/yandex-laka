package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.yandexlavka.config.SalaryConfig;
import ru.yandex.yandexlavka.config.validation.FirstOrder;
import ru.yandex.yandexlavka.config.validation.SecondOrder;
import ru.yandex.yandexlavka.config.validation.TimeWindowConstraint;
import ru.yandex.yandexlavka.model.serializers.RegionListSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Entity
@Table(name = "couriers")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "groupOrders")
@GroupSequence({CourierDto.class, FirstOrder.class, SecondOrder.class})
public class CourierDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("courier_id")
    private long id;

    @Valid
    @JsonProperty("courier_type")
    @Column(name = "courier_type", nullable = false)
    private CourierTypeEnum courierType;

    @Valid
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "courier_regions",
            joinColumns = {@JoinColumn(name = "courier_id")},
            inverseJoinColumns = {@JoinColumn(name = "region_id")}
    )
    @JsonProperty("regions")
    @JsonSerialize(using = RegionListSerializer.class)
    @OrderBy(value = "id")
    @Column(name = "region_id", nullable = false)
    private List<Region> regions = new ArrayList<>();

    @Valid
    @ElementCollection
    @CollectionTable(name = "courier_working_hours",
            joinColumns = @JoinColumn(name = "courier_id"))
    @JsonProperty("working_hours")
    @Column(name = "working_hours", nullable = false)
    private
    List<
            @Valid
            @Pattern(regexp = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]",
                    groups = FirstOrder.class)
            @TimeWindowConstraint(groups = SecondOrder.class)
                    String>
            workingHours = new ArrayList<>();
    @Valid
    @JsonProperty("rating")
    @Column(name = "rating")
    private int rating;

    @Valid
    @JsonProperty("earnings")
    @Column(name = "earnings")
    private int earnings;

    @Valid
    @OneToMany(mappedBy = "courierDto", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference
    private List<GroupOrders> groupOrders = new ArrayList<>();

    public CourierDto() {

    }

    public void calculateEarnings(SalaryConfig salaryConfig, List<OrderDto> orders) {
        Map<String, Integer> factors = salaryConfig.getEarningsFactors();
        Integer factor = factors.get(this.getCourierType().toString().toLowerCase(Locale.ROOT));
        this.earnings = orders.stream().mapToInt(OrderDto::getCost).sum() * factor;
    }

    public void calculateRating(SalaryConfig salaryConfig, int ordersSize, LocalDate startDate,
                                LocalDate endDate) {
        Map<String, Integer> factors = salaryConfig.getRatingFactors();
        Integer factor = factors.get(this.getCourierType().toString().toLowerCase(Locale.ROOT));
        Duration between = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());
        int hours = (int) between.dividedBy(Duration.ofHours(1));
        this.rating = factor * ordersSize / hours;
    }

    public enum CourierTypeEnum {
        FOOT,
        BIKE,
        AUTO
    }

}
