package ru.yandex.yandexlavka.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString(exclude = "orders")
@EqualsAndHashCode(of = "id")
@Table(name = "group_orders")
public class GroupOrders {

    @JsonProperty("group_order_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonProperty("date")
    @Valid
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Valid
    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", referencedColumnName = "id")
    @JsonIgnore
    @JsonBackReference
    private CourierDto courierDto;

    @Valid
    @OneToMany(mappedBy = "groupOrders", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderDto> orders;

    public GroupOrders() {
    }

    public GroupOrders(LocalDate date, CourierDto courierDto) {
        this.date = date;
        this.courierDto = courierDto;
    }

    public void addOrder(OrderDto orderDto) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(orderDto);
    }

}
