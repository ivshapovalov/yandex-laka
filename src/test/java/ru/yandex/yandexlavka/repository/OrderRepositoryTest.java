package ru.yandex.yandexlavka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderRepositoryTest extends CommonTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GroupOrdersRepository groupOrdersRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    public void getOrdersWhenNoOneOrderExistsExecutedCorrectly() {
        int offset = 0;
        int limit = 1;
        List<OrderDto> orders = orderRepository.findAll(offset, limit);
        assertThat(orders).isEmpty();
    }

    @Test
    public void getOrdersWhenOneOrderExistsExecutedCorrectly() {
        Region region1 = new Region(1);
        region1 = regionRepository.saveAndFlush(region1);
        OrderDto orderDto = new OrderDto();
        orderDto.setCost(100);
        orderDto.setWeight(100.0F);
        orderDto.setRegion(region1);
        orderDto.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));

        orderRepository.saveAndFlush(orderDto);

        int offset = 0;
        int limit = 1;
        List<OrderDto> orders = orderRepository.findAll(offset, limit);
        assertEquals(1, orders.size());
        assertEquals(orderDto, orders.get(0));
    }

    @Test
    public void getOrdersWhenThreeOrdersExistsAndOffset1Limit2ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0F);
        orderDto1.setRegion(region1);
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderRepository.saveAndFlush(orderDto1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setCost(200);
        orderDto2.setWeight(200.0F);
        orderDto2.setRegion(region2);
        orderDto2.setDeliveryHours(List.of("15:00-18:00", "08:00-12:00"));
        orderRepository.saveAndFlush(orderDto2);

        OrderDto orderDto3 = new OrderDto();
        orderDto3.setCost(300);
        orderDto3.setWeight(300.0F);
        orderDto3.setRegion(region3);
        orderDto3.setDeliveryHours(List.of("19:00-23:00", "06:00-18:00"));
        orderRepository.saveAndFlush(orderDto3);

        List<OrderDto> expected = new ArrayList<>(Arrays.asList(orderDto2, orderDto3));

        int offset = 1;
        int limit = 2;
        List<OrderDto> actual = orderRepository.findAll(offset, limit);
        assertEquals(2, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getOrdersWhenThreeOrdersExistAndOffset0Limit2ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0F);
        orderDto1.setRegion(region1);
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderRepository.saveAndFlush(orderDto1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setCost(200);
        orderDto2.setWeight(200.0F);
        orderDto2.setRegion(region2);
        orderDto2.setDeliveryHours(List.of("15:00-18:00", "08:00-12:00"));
        orderRepository.saveAndFlush(orderDto2);

        OrderDto orderDto3 = new OrderDto();
        orderDto3.setCost(300);
        orderDto3.setWeight(300.0F);
        orderDto3.setRegion(region3);
        orderDto3.setDeliveryHours(List.of("19:00-23:00", "06:00-18:00"));
        orderRepository.saveAndFlush(orderDto3);

        List<OrderDto> expected = new ArrayList<>(Arrays.asList(orderDto1, orderDto2));

        int offset = 0;
        int limit = 2;
        List<OrderDto> actual = orderRepository.findAll(offset, limit);
        assertEquals(2, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getOrdersWhenThreeOrdersExistAndOffset2Limit3ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0F);
        orderDto1.setRegion(region1);
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderRepository.saveAndFlush(orderDto1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setCost(200);
        orderDto2.setWeight(200.0F);
        orderDto2.setRegion(region2);
        orderDto2.setDeliveryHours(List.of("15:00-18:00", "08:00-12:00"));
        orderRepository.saveAndFlush(orderDto2);

        OrderDto orderDto3 = new OrderDto();
        orderDto3.setCost(300);
        orderDto3.setWeight(300.0F);
        orderDto3.setRegion(region3);
        orderDto3.setDeliveryHours(List.of("19:00-23:00", "06:00-18:00"));
        orderRepository.saveAndFlush(orderDto3);

        List<OrderDto> expected = new ArrayList<>(Arrays.asList(orderDto3));

        int offset = 2;
        int limit = 3;
        List<OrderDto> actual = orderRepository.findAll(offset, limit);
        assertEquals(1, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getOrdersWhenThreeOrdersExistAndOffset3Limit1ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0F);
        orderDto1.setRegion(region1);
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderRepository.saveAndFlush(orderDto1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setCost(200);
        orderDto2.setWeight(200.0F);
        orderDto2.setRegion(region2);
        orderDto2.setDeliveryHours(List.of("15:00-18:00", "08:00-12:00"));
        orderRepository.saveAndFlush(orderDto2);

        OrderDto orderDto3 = new OrderDto();
        orderDto3.setCost(300);
        orderDto3.setWeight(300.0F);
        orderDto3.setRegion(region3);
        orderDto3.setDeliveryHours(List.of("19:00-23:00", "06:00-18:00"));
        orderRepository.saveAndFlush(orderDto3);

        int offset = 3;
        int limit = 1;
        List<OrderDto> actual = orderRepository.findAll(offset, limit);
        assertEquals(0, actual.size());
    }

    @Test
    public void getOrderByIdWhenOrderNotExistsExecutedCorrectly() {
        long id = 1;
        Optional<OrderDto> courier = orderRepository.findById(id);
        assertTrue(courier.isEmpty());
    }

    @Test
    public void getOrderByIdWhenOrderExistsExecutedCorrectly() throws InterruptedException {
        Region region1 = new Region(1);

        long id = 1;
        OrderDto expected = new OrderDto();
        expected.setId(id);
        expected.setCost(100);
        expected.setWeight(100.0F);
        expected.setRegion(region1);
        expected.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderRepository.saveAndFlush(expected);

        Optional<OrderDto> actual = orderRepository.findById(id);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    public void findOrdersByCourierWhereCompletedTimeBetweenDatesNoHasOrdersExecutedCorrectly() throws InterruptedException {
        OffsetDateTime startOffsetDateTime = OffsetDateTime.parse("2023-03-20T00:00:00.000+00");
        OffsetDateTime endOffsetDateTime = OffsetDateTime.parse("2023-03-21T00:00:00.000+00");

        GroupOrders groupOrders = new GroupOrders();
        List<OrderDto> ordersBefore = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-19T01:00:00.000+00"));
            return orderDto;
        }).collect(Collectors.toList());
        List<OrderDto> ordersAfter = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-25T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        List<OrderDto> ordersAll = new ArrayList<>(ordersBefore);
        ordersAll.addAll(ordersAfter);

        groupOrders.setDate(LocalDate.now());
        groupOrders.setOrders(ordersAll);
        groupOrdersRepository.saveAndFlush(groupOrders);

        List<OrderDto> orders =
                orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                        List.of(groupOrders),
                        startOffsetDateTime,
                        endOffsetDateTime);
        assertEquals(0, orders.size());
    }

    @Test
    public void findOrdersByCourierWhereCompletedTimeBetweenDatesWhenAllOrdersInPeriodExecutedCorrectly() throws InterruptedException {
        OffsetDateTime startOffsetDateTime = OffsetDateTime.parse("2023-03-20T00:00:00.000+00");
        OffsetDateTime endOffsetDateTime = OffsetDateTime.parse("2023-03-21T00:00:00.000+00");

        Region region1 = new Region(1);

        GroupOrders groupOrders = new GroupOrders();

        List<OrderDto> completedOrders = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region1);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders.setDate(LocalDate.now());
        groupOrders.setOrders(completedOrders);
        groupOrdersRepository.saveAndFlush(groupOrders);

        List<OrderDto> orders =
                orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                        List.of(groupOrders),
                        startOffsetDateTime,
                        endOffsetDateTime);
        assertEquals(completedOrders, orders);
    }

    @Test
    public void getCourierMetaInfoWhenCourierHasPartOfCompletedOrdersInPeriodExecutedCorrectly() throws InterruptedException {
        OffsetDateTime startOffsetDateTime = OffsetDateTime.parse("2023-03-20T00:00:00.000+00");
        OffsetDateTime endOffsetDateTime = OffsetDateTime.parse("2023-03-21T00:00:00.000+00");

        GroupOrders groupOrders = new GroupOrders();

        List<OrderDto> ordersBeforePeriod = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-19T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        List<OrderDto> ordersInPeriod = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        List<OrderDto> ordersAfterPeriod = IntStream.rangeClosed(49, 72).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-25T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        List<OrderDto> ordersAll = new ArrayList<>();
        ordersAll.addAll(ordersBeforePeriod);
        ordersAll.addAll(ordersInPeriod);
        ordersAll.addAll(ordersAfterPeriod);

        groupOrders.setDate(LocalDate.now());
        groupOrders.setOrders(ordersAll);
        groupOrdersRepository.saveAndFlush(groupOrders);

        List<OrderDto> orders =
                orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                        List.of(groupOrders),
                        startOffsetDateTime,
                        endOffsetDateTime);
        assertEquals(ordersInPeriod, orders);
    }
}
