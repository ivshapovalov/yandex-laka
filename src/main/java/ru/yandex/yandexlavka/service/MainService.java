package ru.yandex.yandexlavka.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.yandexlavka.config.SalaryConfig;
import ru.yandex.yandexlavka.exceptions.CourierNotFoundException;
import ru.yandex.yandexlavka.exceptions.CourierOrderNotFoundException;
import ru.yandex.yandexlavka.exceptions.OrderAlreadyCompletedException;
import ru.yandex.yandexlavka.exceptions.OrderNotFoundException;
import ru.yandex.yandexlavka.model.dto.CompleteOrderDto;
import ru.yandex.yandexlavka.model.dto.CompleteOrderRequest;
import ru.yandex.yandexlavka.model.dto.CouriersGroupOrders;
import ru.yandex.yandexlavka.model.dto.CreateCourierRequest;
import ru.yandex.yandexlavka.model.dto.CreateOrderRequest;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.repository.CourierRepository;
import ru.yandex.yandexlavka.repository.GroupOrdersRepository;
import ru.yandex.yandexlavka.repository.OrderRepository;
import ru.yandex.yandexlavka.repository.RegionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MainService {

    private final VrpService vrpService;
    private final SalaryConfig salaryConfig;
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final RegionRepository regionRepository;
    private final GroupOrdersRepository groupOrdersRepository;

    @Transactional
    public List<CourierDto> createCouriers(CreateCourierRequest createCourierRequest) {
        List<CourierDto> couriers = createCourierRequest.getCouriers().stream()
                .map(createCourierDto -> {
                    List<Region> newRegions = new ArrayList<>();
                    List<Region> oldRegions = new ArrayList<>();

                    createCourierDto.getRegions().forEach(regionId -> {
                        Optional<Region> regionFound = regionRepository.findById(regionId);
                        if (regionFound.isEmpty()) {
                            newRegions.add(new Region(regionId));
                        } else {
                            oldRegions.add(regionFound.get());
                        }
                    });
                    List<Region> regions = new ArrayList<>(oldRegions);
                    if (!newRegions.isEmpty()) {
                        regions.addAll(regionRepository.saveAll(newRegions));
                    }
                    return createCourierDto.toCourierDto(regions);
                })
                .collect(Collectors.toList());
        courierRepository.saveAll(couriers);
        return couriers;
    }

    public CourierDto getCourierById(Long courierId) {
        return courierRepository.findById(courierId).orElseThrow(() -> new CourierNotFoundException(courierId));
    }

    public List<CourierDto> getCouriers(int offset, int limit) {
        return courierRepository.findAll(offset, limit);
    }

    public List<OrderDto> getOrders(int offset, int limit) {
        return orderRepository.findAll(offset, limit);
    }

    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public List<OrderDto> createOrders(CreateOrderRequest createOrderRequest) {
        List<OrderDto> orders = createOrderRequest.getOrders().stream()
                .map(createOrderDto -> {
                    int regionId = createOrderDto.getRegion();
                    Optional<Region> regionFound = regionRepository.findById(regionId);
                    Region region = new Region(regionId);
                    if (regionFound.isEmpty()) {
                        regionRepository.save(region);
                    } else {
                        region = regionFound.get();
                    }
                    return createOrderDto.toOrderDto(region);
                })
                .collect(Collectors.toList());
        return orderRepository.saveAll(orders);
    }

    @Transactional
    public List<OrderDto> completeOrder(CompleteOrderRequest completeOrderRequest) {
        List<OrderDto> completedOrders = new ArrayList<>();
        for (CompleteOrderDto completeOrder : completeOrderRequest.getCompleteOrders()) {
            OrderDto orderDto = orderRepository.findById(completeOrder.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(completeOrder.getOrderId()));
            if (orderDto.getCompletedTime() != null) {
                throw new OrderAlreadyCompletedException(completeOrder.getOrderId());
            }
            CourierDto courierDto = courierRepository.findById(completeOrder.getCourierId())
                    .orElseThrow(() -> new CourierNotFoundException(completeOrder.getCourierId()));
            if (orderDto.getGroupOrders() == null || orderDto.getGroupOrders().getCourierDto() == null ||
                    !courierDto.equals(orderDto.getGroupOrders().getCourierDto())) {
                throw new CourierOrderNotFoundException(completeOrder.getCourierId(), completeOrder.getOrderId());
            } else {
                orderDto.setCompletedTime(completeOrder.getCompleteTime());
                completedOrders.add(orderDto);
            }
        }
        orderRepository.saveAll(completedOrders);
        return completedOrders;
    }

    public OrderAssignResponse getCouriersAssignments(Long courierId, LocalDate date) {
        LocalDate currentDate = (date == null ? LocalDate.now() : date);
        if (courierId != null) {
            return getSpecificCouriersAssignments(courierId, currentDate);
        } else {
            return getAllCouriersAssignments(currentDate);
        }
    }

    private OrderAssignResponse getSpecificCouriersAssignments(Long courierId, LocalDate currentDate) {
        List<GroupOrders> groupOrdersList =
                groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, currentDate);
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId, groupOrdersList);
        return new OrderAssignResponse(currentDate, List.of(couriersGroupOrders));
    }

    private OrderAssignResponse getAllCouriersAssignments(LocalDate currentDate) {
        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByDateEquals(currentDate);
        Map<CourierDto, List<GroupOrders>> groupOrdersMap = groupOrdersList.stream()
                .collect(Collectors.groupingBy(GroupOrders::getCourierDto));

        List<CouriersGroupOrders> couriersGroupOrdersList = groupOrdersMap.entrySet().stream()
                .sorted((o1, o2) -> (int) (o1.getKey().getId() - o2.getKey().getId()))
                .map(entry -> new CouriersGroupOrders(entry.getKey().getId(),
                        entry.getValue().stream()
                                .sorted((g1, g2) -> (int) (g1.getId() - g2.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return new OrderAssignResponse(currentDate, couriersGroupOrdersList);
    }

    public CourierDto getCourierMetaInfo(long courierId, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDate, LocalTime.MIN, ZoneOffset.UTC);
        CourierDto courierDto =
                courierRepository.findById(courierId).orElseThrow(() -> new CourierNotFoundException(courierId));
        List<OrderDto> orders =
                orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                        courierDto.getGroupOrders(),
                        startOffsetDateTime,
                        endOffsetDateTime
                );
        if (orders.size() > 0) {
            courierDto.calculateEarnings(salaryConfig, orders);
            courierDto.calculateRating(salaryConfig, orders.size(), startDate, endDate);
        }
        return courierDto;
    }

    public OrderAssignResponse orderAssign(LocalDate date) {
        LocalDate currentDate = (date == null) ? LocalDate.now() : date;

        List<OrderDto> orders = orderRepository.findAllByCompletedTimeIsNull();
        List<CourierDto> couriers = courierRepository.findAll(0, Integer.MAX_VALUE);
        OrderAssignResponse orderAssignResponse;
        if (couriers.size() > 0 && orders.size() > 0) {
            orderAssignResponse = vrpService.solve(currentDate, couriers, orders);
            List<GroupOrders> groupOrdersList = orderAssignResponse.getCouriersGroupOrdersList().stream()
                    .flatMap(couriersGroupOrders -> couriersGroupOrders.getGroupOrders().stream())
                    .collect(Collectors.toList());
            groupOrdersRepository.saveAllAndFlush(groupOrdersList);
        } else {
            orderAssignResponse = new OrderAssignResponse(currentDate, new ArrayList<>());
        }
        return orderAssignResponse;
    }
}
