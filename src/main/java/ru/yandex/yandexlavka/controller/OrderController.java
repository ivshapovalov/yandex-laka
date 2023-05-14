package ru.yandex.yandexlavka.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.yandexlavka.model.dto.CompleteOrderRequest;
import ru.yandex.yandexlavka.model.dto.CreateOrderRequest;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.service.MainService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class OrderController {

    private final MainService mainService;

    public OrderController(MainService mainService) {
        this.mainService = mainService;
        System.out.println();
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/orders",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    @RateLimiter(name = "createOrderRateLimiter")
    ResponseEntity<List<OrderDto>> createOrder(
            @NotNull @Valid @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(mainService.createOrders(createOrderRequest));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/orders",
            produces = {"application/json"}
    )
    @RateLimiter(name = "getOrdersRateLimiter")
    ResponseEntity<List<OrderDto>> getOrders(
            @Valid @Range(min = 0, max = Integer.MAX_VALUE) int offset,
            @RequestParam(name = "limit", defaultValue = "1", required = false)
            @Valid @Range(min = 1, max = Integer.MAX_VALUE) int limit
    ) {
        return ResponseEntity.ok(mainService.getOrders(offset, limit));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/orders/{order_id}",
            produces = {"application/json"}
    )
    @RateLimiter(name = "getOrderRateLimiter")
    ResponseEntity<OrderDto> getOrder(@PathVariable("order_id") @Valid long orderId) {
        return ResponseEntity.ok(mainService.getOrderById(orderId));
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/orders/complete",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    @RateLimiter(name = "completeOrderRateLimiter")
    ResponseEntity<List<OrderDto>> completeOrder(@NotNull @Valid @RequestBody CompleteOrderRequest completeOrderRequest) {
        return ResponseEntity.ok(mainService.completeOrder(completeOrderRequest));
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/orders/assign",
            produces = {"application/json"}
    )
    @RateLimiter(name = "assignOrderRateLimiter")
    ResponseEntity<List<OrderAssignResponse>> ordersAssign(
            @Valid @RequestParam(value = "date", required = false) final LocalDate date) {
        OrderAssignResponse orderAssignResponse = mainService.orderAssign(date);
        return ResponseEntity.created(URI.create("/couriers/assignments")).body(List.of(orderAssignResponse));
    }

}
