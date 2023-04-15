package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "group_orders")
public class GroupOrders {

    @JsonProperty("group_order_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonProperty("date")
    @Valid
    private LocalDate date;

    @Valid
    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", referencedColumnName = "id")
    @JsonIgnore
    @JsonManagedReference
    private CourierDto courierDto;

    @Valid
    @OneToMany(mappedBy = "groupOrders", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
//    @JoinColumn(name = "group_order_id")
//    @JoinTable(
//            name = "group_orders_content",
//            joinColumns = {@JoinColumn(name = "group_order_id", referencedColumnName = "id")},
//            inverseJoinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")}
//    )
//    @JsonIgnore
    @JsonManagedReference
    private List<OrderDto> orders;

    public GroupOrders() {
    }

    public void addOrder(OrderDto orderDto) {
        if (this.orders != null) {
            this.orders.add(orderDto);
        }
    }

}
