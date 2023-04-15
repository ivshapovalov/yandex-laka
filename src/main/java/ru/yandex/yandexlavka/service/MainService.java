package ru.yandex.yandexlavka.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.yandexlavka.exceptions.CourierNotFoundException;
import ru.yandex.yandexlavka.exceptions.CourierOrderNotFoundException;
import ru.yandex.yandexlavka.exceptions.OrderAlreadyCompletedException;
import ru.yandex.yandexlavka.exceptions.OrderImpossibleException;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MainService {

    private CourierRepository courierRepository;
    private OrderRepository orderRepository;
    private RegionRepository regionRepository;
    private GroupOrdersRepository groupOrdersRepository;

    public MainService(CourierRepository courierRepository,
                       OrderRepository orderRepository,
                       RegionRepository regionRepository,
                       GroupOrdersRepository groupOrdersRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
        this.regionRepository = regionRepository;
        this.groupOrdersRepository = groupOrdersRepository;
    }

    @Transactional
    public List<CourierDto> createCouriers(CreateCourierRequest createCourierRequest) {
        List<CourierDto> couriers = createCourierRequest.getCouriers().stream()
                .map(createCourierDto -> {
                    List<Region> newRegions = new ArrayList<>();
                    List<Region> oldRegions = new ArrayList<>();

                    createCourierDto.getRegions().stream().forEach(regionId -> {
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
                    CourierDto courierDto = createCourierDto.toCourierDto(regions);
                    return courierDto;
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

    @Transactional
    public List<OrderAssignResponse> orderAssign(LocalDate date) {
        LocalDate currentDate = date == null ? LocalDate.now() : date;
        //simple straightforward method
        List<OrderAssignResponse> orderAssignResponse = distributeOrdersSimple(currentDate);
        return orderAssignResponse;
    }

    private List<OrderAssignResponse> distributeOrdersSimple(LocalDate currentDate) {
        List<OrderDto> orders = orderRepository.findAllByCompletedTimeIsNull();
        List<CourierDto> couriers = courierRepository.findAll(0, Integer.MAX_VALUE);
        List<OrderAssignResponse> response = new ArrayList<>();
        List<GroupOrders> groupOrdersList = new ArrayList<>();
        for (OrderDto order : orders) {
            CourierDto courier = couriers.stream().filter(
                            curCourier -> curCourier.getRegions().contains(order.getRegion()))
                    .findFirst().orElseThrow(() -> new OrderImpossibleException(order.getId()));
            couriers.remove(courier);
            GroupOrders groupOrders = new GroupOrders();
            groupOrders.setDate(currentDate);
            groupOrders.setCourierDto(courier);
            courier.setGroupOrders(Arrays.asList(groupOrders));
            order.setGroupOrders(groupOrders);
            groupOrders.addOrder(order);

            groupOrdersList.add(groupOrders);

            CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders();
            couriersGroupOrders.setCourierId(courier.getId());
            couriersGroupOrders.setGroupOrders(groupOrders);
            OrderAssignResponse orderAssignResponse = new OrderAssignResponse();
            orderAssignResponse.setDate(currentDate);
            orderAssignResponse.setCouriersGroupOrders(Arrays.asList(couriersGroupOrders));
            response.add(orderAssignResponse);

        }
        orderRepository.saveAllAndFlush(orders);
        return response;
    }

    public List<OrderAssignResponse> getCouriersAssignments(Long courierId, LocalDate date) {
        LocalDate currentDate = (date == null ? LocalDate.now() : date);
        if (courierId != null) {
            return getSpecificCouriersAssignments(courierId, currentDate);
        } else {
            return getAllCouriersAssignments(currentDate);
        }
    }

    private List<OrderAssignResponse> getSpecificCouriersAssignments(Long courierId, LocalDate currentDate) {
        List<GroupOrders> groupOrdersList =
                groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, currentDate);
        List<CouriersGroupOrders> courierGroupOrders = groupOrdersList.stream().map(groupOrders -> {
            CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders();
            couriersGroupOrders.setCourierId(courierId);
            couriersGroupOrders.setGroupOrders(groupOrders);
            return couriersGroupOrders;
        }).collect(Collectors.toList());

        OrderAssignResponse orderAssignResponse = new OrderAssignResponse();
        orderAssignResponse.setDate(currentDate);
        orderAssignResponse.setCouriersGroupOrders(courierGroupOrders);
        return List.of(orderAssignResponse);
    }

    private List<OrderAssignResponse> getAllCouriersAssignments(LocalDate currentDate) {
        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByDateEquals(currentDate);
        Map<CourierDto, List<GroupOrders>> groupOrdersMap = groupOrdersList.stream()
                .collect(Collectors.groupingBy(GroupOrders::getCourierDto));
        List<OrderAssignResponse> orderAssignResponses =
                groupOrdersMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.comparingLong(CourierDto::getId)))
                        .map(entry -> {
                            List<CouriersGroupOrders> courierGroupOrders =
                                    entry.getValue().stream()
                                            .sorted(Comparator.comparingLong(GroupOrders::getId))
                                            .map(groupOrders -> {
                                                CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders();
                                                couriersGroupOrders.setCourierId(groupOrders.getCourierDto().getId());
                                                couriersGroupOrders.setGroupOrders(groupOrders);
                                                return couriersGroupOrders;
                                            }).collect(Collectors.toList());
                            OrderAssignResponse orderAssignResponse = new OrderAssignResponse();
                            orderAssignResponse.setDate(currentDate);
                            orderAssignResponse.setCouriersGroupOrders(courierGroupOrders);
                            return orderAssignResponse;
                        }).collect(Collectors.toList());
        return orderAssignResponses;
    }

    public CourierDto getCourierMetaInfo(long courierId, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDate, LocalTime.MIN, ZoneOffset.UTC);
        CourierDto courierDto =
                courierRepository.findById(courierId).orElseThrow(() -> new CourierNotFoundException(courierId));
//        List<Long> courierOrderIds =
//                courierDto.getGroupOrders().stream()
//                        .flatMap(el -> el.getOrders().stream())
//                        .map(el->el.getId())
//                        .collect(Collectors.toList());
//
//        List<OrderDto> orders =
//                orderRepository.findAllByIdAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
//                        courierOrderIds,
//                        startOffsetDateTime,
//                        endOffsetDateTime
//                );
        List<OrderDto> orders =
                orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                        courierDto.getGroupOrders(),
                        startOffsetDateTime,
                        endOffsetDateTime
                );
        if (orders.size() > 0) {
            courierDto.calculateEarnings(orders);
            courierDto.calculateRating(orders, startDate, endDate);
        }
        return courierDto;
    }
}
