package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.yandexlavka.model.serializers.RegionListSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "couriers")
@Data
public class CourierDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("courier_id")
    private long id;

    @Valid
    @JsonProperty("courier_type")
    @Column(name = "courier_type")
    private CourierTypeEnum courierType;

    @Valid
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(
            name = "courier_regions",
            joinColumns = {@JoinColumn(name = "courier_id")},
            inverseJoinColumns = {@JoinColumn(name = "region_id")}
    )
    @JsonProperty("regions")
    @JsonSerialize(using = RegionListSerializer.class)
    @OrderBy(value = "id")
    @Column(name = "region_id")
    private List<Region> regions = new ArrayList<>();

    @Valid
    @ElementCollection
    @CollectionTable(name = "courier_working_hours",
            joinColumns = @JoinColumn(name = "courier_id"))
    @JsonProperty("working_hours")
    @OrderBy(value = "working_hours")
    @Column(name = "working_hours")
    private
    List<@Valid @Pattern(regexp = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]") String>
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
    @OneToMany(mappedBy = "courierDto",cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
//    @JoinColumn(name = "courier_id", referencedColumnName = "id")
//    @JoinTable(
//            name = "courier_group_orders",
//            joinColumns = {@JoinColumn(name = "courier_id", referencedColumnName = "id")},
//            inverseJoinColumns = {@JoinColumn(name = "group_order_id", referencedColumnName = "id")}
//    )
    @JsonIgnore
    @JsonBackReference
    private List<GroupOrders> groupOrders=new ArrayList<>();

    public CourierDto() {

    }

    public void calculateEarnings(List<OrderDto> orders) {
        Map<CourierTypeEnum, Integer> rates = new HashMap<>();
        rates.put(CourierTypeEnum.FOOT, 2);
        rates.put(CourierTypeEnum.BIKE, 3);
        rates.put(CourierTypeEnum.AUTO, 4);
        Integer rate = rates.get(this.getCourierType());
        this.earnings = orders.stream().mapToInt(order -> order.getCost()).sum()*rate;
    }

    public void calculateRating(List<OrderDto> orders, LocalDate startDate, LocalDate endDate) {
        Map<CourierTypeEnum, Integer> rates = new HashMap<>();
        rates.put(CourierTypeEnum.FOOT, 3);
        rates.put(CourierTypeEnum.BIKE, 2);
        rates.put(CourierTypeEnum.AUTO, 1);
        Integer rate = rates.get(this.getCourierType());

        Duration between = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());
        int hours = (int) between.dividedBy(Duration.ofHours(1));
        int rating = orders.size() / hours * rate;
        this.rating = rating;
    }

    public enum CourierTypeEnum {
        FOOT,
        BIKE,
        AUTO;
    }

}
