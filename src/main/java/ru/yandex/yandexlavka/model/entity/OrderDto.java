package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.yandexlavka.model.serializers.RegionSerializer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@EntityListeners(AuditingEntityListener.class)
public class OrderDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("order_id")
    @Column(name = "id")
    private long id;

    @JsonProperty("weight")
    @Column(name = "weight")
    @Valid
    private float weight;

    @JsonProperty("region")
    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Valid
    @JsonSerialize(using = RegionSerializer.class)
    private Region region;

    @JsonProperty("delivery_hours")
    @Valid
    @ElementCollection
    @CollectionTable(name = "order_delivery_hours",
            joinColumns = @JoinColumn(name = "order_id"))
    @OrderBy(value = "delivery_hours")
    @Column(name = "delivery_hours")
    private
    List<@Valid @Pattern(regexp = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]") String>
            deliveryHours = new ArrayList<>();

    @JsonProperty("cost")
    @Column(name = "cost")
    private int cost;

    @JsonProperty("completed_time")
    @Column(name = "completed_time")
    @Valid
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime completedTime;

//    @Valid
//    @ManyToOne(cascade = {CascadeType.ALL},fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "courier_orders",
//            joinColumns = {@JoinColumn(name = "order_id",referencedColumnName = "id")},
//            inverseJoinColumns = {@JoinColumn(name = "courier_id",referencedColumnName = "id")}
//    )
//    @JsonIgnore
//    @JsonBackReference
//    private CourierDto courierDto;

    @Valid
    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_order_id", referencedColumnName = "id")
//    @JoinTable(
//            name = "group_orders_content",
//            joinColumns = {@JoinColumn(name = "order_id",referencedColumnName = "id")},
//            inverseJoinColumns = {@JoinColumn(name = "group_order_id",referencedColumnName = "id")}
//    )
    @JsonIgnore
    @JsonBackReference
    private GroupOrders groupOrders;

    public OrderDto() {

    }

    @JsonIgnore
    public long getGroupOrdersCourierId() {
        CourierDto courierDto = this.groupOrders.getCourierDto();
        return courierDto.getId();
    }
}
