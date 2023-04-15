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
import ru.yandex.yandexlavka.model.dto.CreateCourierRequest;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.service.MainService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class CourierController {

    private MainService mainService;

    public CourierController(MainService mainService) {

        this.mainService = mainService;
        System.out.println();
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/couriers",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    @RateLimiter(name = "createCouriersRateLimiter")
    ResponseEntity<List<CourierDto>> createCouriers(
            @NotNull @Valid @RequestBody CreateCourierRequest createCourierRequest
    ) {
        return ResponseEntity.ok(mainService.createCouriers(createCourierRequest));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/couriers/{courier_id}",
            produces = {"application/json"}
    )
    @RateLimiter(name = "getCourierByIdRateLimiter")
    ResponseEntity<CourierDto> getCourier(@PathVariable("courier_id") long courierId) {
        return ResponseEntity.ok(mainService.getCourierById(courierId));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/couriers",
            produces = {"application/json"}
    )
    @RateLimiter(name = "getCouriersRateLimiter")
    ResponseEntity<List<CourierDto>> getCouriers(
            @RequestParam(value = "offset", name = "offset", defaultValue = "0", required = false)
            @Valid @Range(min = 0, max = Integer.MAX_VALUE) int offset,
            @RequestParam(name = "limit", defaultValue = "1", required = false)
            @Valid @Range(min = 1, max = Integer.MAX_VALUE) int limit
    ) {
        return ResponseEntity.ok(mainService.getCouriers(offset, limit));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/couriers/meta-info/{courier_id}",
            produces = {"application/json"}
    )
    @RateLimiter(name = "getCourierMetaInfoRateLimiter")
    ResponseEntity<CourierDto> getCourierMetaInfo(
            @PathVariable("courier_id") long courierId,
            @RequestParam(value = "start_date") @Valid LocalDate startDate,
            @RequestParam(value = "end_date") @Valid LocalDate endDate) {
        return ResponseEntity.ok(mainService.getCourierMetaInfo(courierId, startDate, endDate));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/couriers/assignments",
            produces = {"application/json"}
    )
    @RateLimiter(name = "couriersAssignmentsRateLimiter")
    ResponseEntity<List<OrderAssignResponse>> couriersAssignments(
            @Valid @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "courier_id", required = false) Long courierId
    ) {
        return ResponseEntity.ok(mainService.getCouriersAssignments(courierId,date));
    }

}
