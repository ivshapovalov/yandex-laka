package ru.yandex.yandexlavka.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.exceptions.CourierNotFoundException;
import ru.yandex.yandexlavka.exceptions.CourierOrderNotFoundException;
import ru.yandex.yandexlavka.exceptions.OrderAlreadyCompletedException;
import ru.yandex.yandexlavka.exceptions.OrderNotFoundException;
import ru.yandex.yandexlavka.model.dto.CompleteOrderDto;
import ru.yandex.yandexlavka.model.dto.CompleteOrderRequest;
import ru.yandex.yandexlavka.model.dto.CouriersGroupOrders;
import ru.yandex.yandexlavka.model.dto.CreateCourierDto;
import ru.yandex.yandexlavka.model.dto.CreateCourierRequest;
import ru.yandex.yandexlavka.model.dto.CreateOrderDto;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class MainServiceTest extends CommonTest {

    @Autowired
    @InjectMocks
    private MainService mainService;

    @MockBean
    private CourierRepository courierRepository;

    @MockBean
    private RegionRepository regionRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private GroupOrdersRepository groupOrdersRepository;

    @MockBean
    private VrpService vrpService;

    @Test
    public void createCouriersWhenOneCourierAndAllRegionsAreNewExecutedCorrectly() {
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto = new CreateCourierDto();
        createCourierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto.setRegions(new HashSet<>(regionIds));
        createCourierDto.setWorkingHours(Set.of("09:00-18:00", "19:00-21:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> newRegions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto = createCourierDto.toCourierDto(newRegions);
        List<CourierDto> expected = Arrays.asList(courierDto);
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.findById(2)).thenReturn(Optional.empty());
        when(regionRepository.findById(3)).thenReturn(Optional.empty());
        when(regionRepository.saveAll(any(List.class))).thenReturn(newRegions);
        when(courierRepository.saveAll(any(List.class))).thenReturn(expected);

        List<CourierDto> actual = mainService.createCouriers(createCourierRequest);

        verify(regionRepository).findById(1);
        verify(regionRepository).findById(2);
        verify(regionRepository).findById(3);
        verify(regionRepository).saveAll(any(List.class));
        ArgumentCaptor<List<CourierDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(courierRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createCouriersWhenOneCourierAndAllRegionsAreOldExecutedCorrectly() {
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto = new CreateCourierDto();
        createCourierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto.setRegions(new HashSet<>(regionIds));
        createCourierDto.setWorkingHours(Set.of("09:00-18:00", "19:00-21:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> oldRegions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto = createCourierDto.toCourierDto(oldRegions);
        List<CourierDto> expected = Arrays.asList(courierDto);
        when(regionRepository.findById(1)).thenReturn(Optional.of(region1));
        when(regionRepository.findById(2)).thenReturn(Optional.of(region2));
        when(regionRepository.findById(3)).thenReturn(Optional.of(region3));
        when(courierRepository.saveAll(any(List.class))).thenReturn(expected);

        List<CourierDto> actual = mainService.createCouriers(createCourierRequest);

        verify(regionRepository).findById(1);
        verify(regionRepository).findById(2);
        verify(regionRepository).findById(3);
        ArgumentCaptor<List<CourierDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(courierRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createOneCourierWhenOneRegionOfThreeIsNewExecutedCorrectly() {
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto = new CreateCourierDto();
        createCourierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto.setRegions(new HashSet<>(regionIds));
        createCourierDto.setWorkingHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> regions = Arrays.asList(region1, region2, region3);
        List<Region> newRegions = Arrays.asList(region1);

        CourierDto courierDto = createCourierDto.toCourierDto(regions);
        List<CourierDto> expected = Arrays.asList(courierDto);
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.findById(2)).thenReturn(Optional.of(region2));
        when(regionRepository.findById(3)).thenReturn(Optional.of(region3));
        when(regionRepository.saveAll(any(List.class))).thenReturn(newRegions);
        when(courierRepository.saveAll(any(List.class))).thenReturn(expected);

        List<CourierDto> actual = mainService.createCouriers(createCourierRequest);

        verify(regionRepository).findById(1);
        verify(regionRepository).findById(2);
        verify(regionRepository).findById(3);
        verify(regionRepository).saveAll(any(List.class));
        ArgumentCaptor<List<CourierDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(courierRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createCouriersWhenTwoCouriersAndAllRegionsAreOldExecutedCorrectly() {
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto1 = new CreateCourierDto();
        createCourierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto1.setRegions(new HashSet<>(regionIds));
        createCourierDto1.setWorkingHours(Set.of("09:00-18:00", "19:00-21:00"));

        CreateCourierDto createCourierDto2 = new CreateCourierDto();
        createCourierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        createCourierDto2.setRegions(new HashSet<>(regionIds));
        createCourierDto2.setWorkingHours(Set.of("19:00-21:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto1, createCourierDto2));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> oldRegions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto1 = createCourierDto1.toCourierDto(oldRegions);
        CourierDto courierDto2 = createCourierDto2.toCourierDto(oldRegions);
        List<CourierDto> expected = Arrays.asList(courierDto1, courierDto2);
        when(regionRepository.findById(1)).thenReturn(Optional.of(region1));
        when(regionRepository.findById(2)).thenReturn(Optional.of(region2));
        when(regionRepository.findById(3)).thenReturn(Optional.of(region3));
        when(regionRepository.findAllById(regionIds)).thenReturn(oldRegions);
        when(courierRepository.saveAll(any(List.class))).thenReturn(expected);

        List<CourierDto> actual = mainService.createCouriers(createCourierRequest);

        verify(regionRepository, times(2)).findById(1);
        verify(regionRepository, times(2)).findById(2);
        verify(regionRepository, times(2)).findById(3);
        verify(courierRepository).saveAll(expected);
        ArgumentCaptor<List<CourierDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(courierRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersWhenNoCouriersReturnEmptyArrayExecutedCorrectly() {
        int offset = 0;
        int limit = 2;

        when(courierRepository.findAll(offset, limit)).thenReturn(new ArrayList<>());
        List<CourierDto> actual = mainService.getCouriers(offset, limit);

        verify(courierRepository).findAll(offset, limit);
        assertIterableEquals(new ArrayList<>(), actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersWhenReturnSeveralCouriersExecutedCorrectly() {
        int offset = 0;
        int limit = 2;

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(new Region(3), new Region(4)));
        courierDto2.setWorkingHours(List.of("09:00-21:00"));

        List<CourierDto> expected = Arrays.asList(courierDto1, courierDto2);

        when(courierRepository.findAll(offset, limit)).thenReturn(expected);
        List<CourierDto> actual = mainService.getCouriers(offset, limit);

        assertEquals(expected, actual);
        verify(courierRepository).findAll(offset, limit);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCourierByIdWhenCourierNotExistsThrowsException() {
        long id = 1;
        when(courierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> {
            mainService.getCourierById(id);
        });

        verify(courierRepository).findById(id);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCourierByIdWhenCourierExistsExecutedCorrectly() {
        long id = 1;
        CourierDto expected = new CourierDto();
        expected.setId(id);
        expected.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        expected.setRegions(Arrays.asList(new Region(1), new Region(2)));
        expected.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        when(courierRepository.findById(id)).thenReturn(Optional.of(expected));

        CourierDto actual = mainService.getCourierById(id);

        assertEquals(expected, actual);
        verify(courierRepository).findById(id);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createOrdersWhenOneOrderAndRegionIsNewExecutedCorrectly() {
        int regionId = 1;
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setCost(10);
        createOrderDto.setWeight(10.05f);
        createOrderDto.setRegion(regionId);
        createOrderDto.setDeliveryHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto));

        Region region1 = new Region(regionId);

        OrderDto orderDto = createOrderDto.toOrderDto(region1);
        List<OrderDto> expected = Arrays.asList(orderDto);
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.save(any(Region.class))).thenReturn(region1);
        when(orderRepository.saveAll(any(List.class))).thenReturn(expected);

        List<OrderDto> actual = mainService.createOrders(createOrderRequest);

        ArgumentCaptor<List<OrderDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(regionRepository).findById(regionId);
        verify(regionRepository).save(any(Region.class));
        verify(orderRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createOrdersWhenOneOrderAndRegionIsOldExecutedCorrectly() {
        int regionId = 1;
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setCost(10);
        createOrderDto.setWeight(10.05f);
        createOrderDto.setRegion(regionId);
        createOrderDto.setDeliveryHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto));

        Region region1 = new Region(regionId);

        OrderDto orderDto = createOrderDto.toOrderDto(region1);
        List<OrderDto> expected = Arrays.asList(orderDto);
        when(regionRepository.findById(1)).thenReturn(Optional.of(region1));
        when(orderRepository.saveAll(any(List.class))).thenReturn(expected);

        List<OrderDto> actual = mainService.createOrders(createOrderRequest);

        ArgumentCaptor<List<OrderDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(regionRepository).findById(regionId);
        verify(orderRepository).saveAll(captor.capture());
        assertIterableEquals(expected, captor.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void createOrdersWhenTwoOrdersAndOneRegionsIsNewExecutedCorrectly() {
        int oldRegionId = 1;
        int newRegionId = 2;
        CreateOrderDto createOrderDto1 = new CreateOrderDto();
        createOrderDto1.setCost(10);
        createOrderDto1.setWeight(10.05f);
        createOrderDto1.setRegion(oldRegionId);
        createOrderDto1.setDeliveryHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateOrderDto createOrderDto2 = new CreateOrderDto();
        createOrderDto2.setCost(10);
        createOrderDto2.setWeight(10.05f);
        createOrderDto2.setRegion(newRegionId);
        createOrderDto2.setDeliveryHours(Set.of("09:00-18:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto1, createOrderDto2));

        Region oldRegion = new Region(oldRegionId);
        Region newRegion = new Region(newRegionId);

        OrderDto orderDto1 = createOrderDto1.toOrderDto(oldRegion);
        OrderDto orderDto2 = createOrderDto2.toOrderDto(newRegion);
        List<OrderDto> expected = Arrays.asList(orderDto1, orderDto2);
        when(regionRepository.findById(oldRegionId)).thenReturn(Optional.of(oldRegion));
        when(regionRepository.findById(newRegionId)).thenReturn(Optional.empty());
        when(regionRepository.save(any(Region.class))).thenReturn(newRegion);
        when(orderRepository.saveAll(any(List.class))).thenReturn(expected);

        List<OrderDto> actual = mainService.createOrders(createOrderRequest);

        ArgumentCaptor<List<OrderDto>> captorOrder = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Region> captorRegion = ArgumentCaptor.forClass(Region.class);
        verify(regionRepository).findById(oldRegionId);
        verify(regionRepository).findById(newRegionId);
        verify(regionRepository).save(captorRegion.capture());
        verify(orderRepository).saveAll(captorOrder.capture());
        assertEquals(newRegionId, captorRegion.getValue().getId());
        assertIterableEquals(expected, captorOrder.getValue());
        assertIterableEquals(expected, actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getOrdersWhenNoOrdersReturnEmptyArrayExecutedCorrectly() {
        int offset = 0;
        int limit = 2;

        when(orderRepository.findAll(offset, limit)).thenReturn(new ArrayList<>());
        List<OrderDto> actual = mainService.getOrders(offset, limit);

        verify(orderRepository).findAll(offset, limit);
        assertIterableEquals(new ArrayList<>(), actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getOrdersWhenReturnSeveralOrdersExecutedCorrectly() {
        int offset = 0;
        int limit = 2;

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0F);
        orderDto1.setRegion(new Region(1));
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setCost(200);
        orderDto2.setWeight(20.05F);
        orderDto2.setRegion(new Region(2));
        orderDto2.setDeliveryHours(List.of("19:00-21:00"));

        List<OrderDto> expected = Arrays.asList(orderDto2, orderDto2);

        when(orderRepository.findAll(offset, limit)).thenReturn(expected);
        List<OrderDto> actual = mainService.getOrders(offset, limit);

        assertEquals(expected, actual);
        verify(orderRepository).findAll(offset, limit);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getOrderByIdWhenOrderNotExistsThrowsException() {
        long id = 1;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            mainService.getOrderById(id);
        });

        verify(orderRepository).findById(id);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getOrderByIdWhenOrderExistsExecutedCorrectly() {
        long id = 1;
        OrderDto expected = new OrderDto();
        expected.setCost(100);
        expected.setWeight(100.0F);
        expected.setRegion(new Region(1));
        expected.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));

        when(orderRepository.findById(id)).thenReturn(Optional.of(expected));

        OrderDto actual = mainService.getOrderById(id);

        assertEquals(expected, actual);
        verify(orderRepository).findById(id);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenOrderNotExistsThrowsException() {
        long courierId = 15;
        long orderId = 3;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenOrderAlreadyCompletedThrowsException() {
        long courierId = 15;
        long orderId = 3;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setCompletedTime(OffsetDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderDto));

        assertThrows(OrderAlreadyCompletedException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenCourierNotExistsThrowsException() {
        long courierId = 15;
        long orderId = 3;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderDto));
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId);
        verify(courierRepository).findById(courierId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenOrderNotAssignedThrowsException() {
        long courierId = 15;
        long orderId = 3;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderDto));
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courierDto));

        assertThrows(CourierOrderNotFoundException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId);
        verify(courierRepository).findById(courierId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenOrderAssignedToAnotherCourierThrowsException() {
        long courierIdOld = 2;
        long courierId = 15;
        long orderId = 1;
        long groupOrderIdOld = 13;
        long groupOrderId = 17;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        CourierDto courierDtoOld = new CourierDto();
        courierDtoOld.setId(courierIdOld);

        GroupOrders groupOrdersOld = new GroupOrders();
        groupOrdersOld.setId(groupOrderIdOld);
        groupOrdersOld.setCourierDto(courierDtoOld);

        GroupOrders groupOrdersNew = new GroupOrders();
        groupOrdersNew.setId(groupOrderId);

        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setGroupOrders(groupOrdersNew);

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderDto));
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courierDto));

        assertThrows(CourierOrderNotFoundException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId);
        verify(courierRepository).findById(courierId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenOneOrderExecutedCorrectly() {
        long courierId = 15;
        long orderId = 3;
        long groupOrderId = 18;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(courierId);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.now());
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto));

        CourierDto courierDto = new CourierDto();
        courierDto.setId(orderId);

        GroupOrders groupOrders = new GroupOrders();
        groupOrders.setId(groupOrderId);
        groupOrders.setCourierDto(courierDto);

        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setGroupOrders(groupOrders);

        List<OrderDto> orders = List.of(orderDto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderDto));
        when(orderRepository.saveAll(any(List.class))).thenReturn(orders);
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courierDto));

        List<OrderDto> actual = mainService.completeOrder(completeOrderRequest);

        OrderDto orderDtoExpected = new OrderDto();
        orderDtoExpected.setId(orderId);
        orderDtoExpected.setGroupOrders(groupOrders);
        orderDtoExpected.setCompletedTime(completeOrderDto.getCompleteTime());

        ArgumentCaptor<List<OrderDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).findById(orderId);
        verify(courierRepository).findById(courierId);
        verify(orderRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertIterableEquals(List.of(orderDtoExpected), captor.getValue());
        assertIterableEquals(List.of(orderDtoExpected), actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenTwoOrdersExecutedCorrectly() {
        long courierId1 = 15;
        long orderId1 = 3;
        CompleteOrderDto completeOrderDto1 = new CompleteOrderDto();
        completeOrderDto1.setCourierId(courierId1);
        completeOrderDto1.setOrderId(orderId1);
        completeOrderDto1.setCompleteTime(OffsetDateTime.now());

        long courierId2 = 14;
        long orderId2 = 2;
        CompleteOrderDto completeOrderDto2 = new CompleteOrderDto();
        completeOrderDto2.setCourierId(courierId2);
        completeOrderDto2.setOrderId(orderId2);
        completeOrderDto2.setCompleteTime(OffsetDateTime.now());

        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto1, completeOrderDto2));

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(orderId1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setId(orderId2);

        long groupOrderId1 = 25;
        long groupOrderId2 = 35;
        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setId(groupOrderId1);
        groupOrders1.setCourierDto(courierDto1);

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setId(groupOrderId2);
        groupOrders2.setCourierDto(courierDto2);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(orderId1);
        orderDto1.setGroupOrders(groupOrders1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setId(orderId2);
        orderDto2.setGroupOrders(groupOrders2);

        List<OrderDto> orders = List.of(orderDto1, orderDto2);

        when(orderRepository.findById(orderId1)).thenReturn(Optional.of(orderDto1));
        when(orderRepository.findById(orderId2)).thenReturn(Optional.of(orderDto2));
        when(orderRepository.saveAll(any(List.class))).thenReturn(orders);
        when(courierRepository.findById(courierId1)).thenReturn(Optional.of(courierDto1));
        when(courierRepository.findById(courierId2)).thenReturn(Optional.of(courierDto2));

        OrderDto orderDto1Expected = new OrderDto();
        orderDto1Expected.setId(orderId1);
        orderDto1Expected.setGroupOrders(groupOrders1);
        orderDto1Expected.setCompletedTime(completeOrderDto1.getCompleteTime());

        OrderDto orderDto2Expected = new OrderDto();
        orderDto2Expected.setId(orderId2);
        orderDto2Expected.setGroupOrders(groupOrders2);
        orderDto2Expected.setCompletedTime(completeOrderDto2.getCompleteTime());

        List<OrderDto> actual = mainService.completeOrder(completeOrderRequest);

        ArgumentCaptor<List<OrderDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).findById(orderId1);
        verify(orderRepository).findById(orderId2);
        verify(courierRepository).findById(courierId1);
        verify(courierRepository).findById(courierId2);
        verify(orderRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertIterableEquals(List.of(orderDto1Expected, orderDto2Expected), captor.getValue());
        assertIterableEquals(List.of(orderDto1Expected, orderDto2Expected), actual);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void completeOrderWhenTwoOrdersAndOneOrderIsInvalidThrowsException() {
        long courierId1 = 15;
        long courierId2 = 14;

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(courierId1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setId(courierId1);

        long groupOrderId1 = 25;
        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setId(groupOrderId1);
        groupOrders1.setCourierDto(courierDto1);

        long orderId1 = 3;
        CompleteOrderDto completeOrderDto1 = new CompleteOrderDto();
        completeOrderDto1.setCourierId(courierId1);
        completeOrderDto1.setOrderId(orderId1);
        completeOrderDto1.setCompleteTime(OffsetDateTime.now());

        long orderId2 = 2;
        CompleteOrderDto completeOrderDto2 = new CompleteOrderDto();
        completeOrderDto2.setCourierId(courierId2);
        completeOrderDto2.setOrderId(orderId2);
        completeOrderDto2.setCompleteTime(OffsetDateTime.now());

        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(List.of(completeOrderDto1, completeOrderDto2));

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(orderId1);
        orderDto1.setGroupOrders(groupOrders1);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setId(orderId2);

        when(orderRepository.findById(orderId1)).thenReturn(Optional.of(orderDto1));
        when(orderRepository.findById(orderId2)).thenReturn(Optional.of(orderDto2));
        when(courierRepository.findById(courierId1)).thenReturn(Optional.of(courierDto1));
        when(courierRepository.findById(courierId2)).thenReturn(Optional.of(courierDto2));

        assertThrows(CourierOrderNotFoundException.class, () -> {
            mainService.completeOrder(completeOrderRequest);
        });

        verify(orderRepository).findById(orderId1);
        verify(orderRepository).findById(orderId2);
        verify(courierRepository).findById(courierId1);
        verify(courierRepository).findById(courierId2);

        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCourierMetaInfoWhenCourierExistsWithOrdersExecutedCorrectly() {
        long courierId = 11;

        LocalDate startDate = LocalDate.parse("2023-03-20");
        LocalDate endDate = LocalDate.parse("2023-03-21");

        OffsetDateTime startOffsetDateTime = OffsetDateTime.parse("2023-03-20T00:00:00.000+00");
        OffsetDateTime endOffsetDateTime = OffsetDateTime.parse("2023-03-21T00:00:00.000+00");

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        long groupOrderId = 25;
        GroupOrders groupOrders = new GroupOrders();
        groupOrders.setId(groupOrderId);
        List<GroupOrders> groupOrdersList = List.of(groupOrders);
        courierDto.setGroupOrders(groupOrdersList);

        List<OrderDto> completedOrders = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setId(ind);
            orderDto.setCost(ind);
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        CourierDto expected = new CourierDto();
        expected.setId(courierId);
        expected.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        expected.setRegions(Arrays.asList(new Region(1), new Region(2)));
        expected.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        expected.setEarnings(900);
        expected.setRating(2);
        expected.setGroupOrders(groupOrdersList);

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courierDto));
        when(orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                groupOrdersList, startOffsetDateTime, endOffsetDateTime)).thenReturn(completedOrders);

        CourierDto actual = mainService.getCourierMetaInfo(courierId, startDate, endDate);

        assertEquals(expected, actual);
        verify(courierRepository).findById(courierId);
        verify(orderRepository).findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                groupOrdersList, startOffsetDateTime, endOffsetDateTime);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCourierMetaInfoWhenCourierHasNoOrdersInPeriodExecutedCorrectly() {
        long courierId = 11;

        LocalDate startDate = LocalDate.parse("2023-03-20");
        LocalDate endDate = LocalDate.parse("2023-03-21");

        OffsetDateTime startOffsetDateTime = OffsetDateTime.parse("2023-03-20T00:00:00.000+00");
        OffsetDateTime endOffsetDateTime = OffsetDateTime.parse("2023-03-21T00:00:00.000+00");

        long groupOrderId = 25;
        GroupOrders groupOrders = new GroupOrders();
        groupOrders.setId(groupOrderId);
        List<GroupOrders> groupOrdersList = List.of(groupOrders);

        List<OrderDto> ordersBefore = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setId(ind);
            orderDto.setCost(ind);
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());
        List<OrderDto> ordersAfter = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setId(ind);
            orderDto.setCost(ind);
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());
        List<OrderDto> ordersAll = new ArrayList<>(ordersBefore);
        ordersAll.addAll(ordersAfter);

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto.setGroupOrders(groupOrdersList);

        CourierDto expected = new CourierDto();
        expected.setId(courierId);
        expected.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        expected.setRegions(Arrays.asList(new Region(1), new Region(2)));
        expected.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        expected.setEarnings(0);
        expected.setRating(0);
        expected.setGroupOrders(groupOrdersList);

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courierDto));
        when(orderRepository.findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                groupOrdersList, startOffsetDateTime, endOffsetDateTime)).thenReturn(new ArrayList<>());

        CourierDto actual = mainService.getCourierMetaInfo(courierId, startDate, endDate);

        assertEquals(expected, actual);
        verify(courierRepository).findById(courierId);
        verify(orderRepository).findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(
                groupOrdersList, startOffsetDateTime, endOffsetDateTime);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCourierMetaInfoWhenCourierNotExistsThrowsException() {
        long courierId = 11;

        LocalDate startDate = LocalDate.parse("2023-03-20");
        LocalDate endDate = LocalDate.parse("2023-03-21");

        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> {
            mainService.getCourierMetaInfo(courierId, startDate, endDate);
        });

        verify(courierRepository).findById(courierId);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersAssignmentsWhenCourierIdSetAndDateSetAndOrdersExistExecutedCorrectly() {
        long courierId = 11;
        LocalDate currentDate = LocalDate.parse("2023-04-14");

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(currentDate);

        List<OrderDto> orders1 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders1);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1.setOrders(orders1);

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setDate(currentDate);

        List<OrderDto> orders2 = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders2);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2.setOrders(orders2);

        List<GroupOrders> groupOrdersList = Arrays.asList(groupOrders1, groupOrders2);

        List<CouriersGroupOrders> courierGroupOrders =
                List.of(new CouriersGroupOrders(courierId, groupOrdersList));

        OrderAssignResponse expected = new OrderAssignResponse(currentDate, courierGroupOrders);

        when(groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, currentDate)).thenReturn(groupOrdersList);

        OrderAssignResponse actual = mainService.getCouriersAssignments(courierId, currentDate);

        assertEquals(expected, actual);
        verify(groupOrdersRepository).findAllByCourierIdEqualsAndDateEquals(courierId, currentDate);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersAssignmentsWhenCourierIdSetAndDateSetAndOrdersEmptyExecutedCorrectly() {
        long courierId = 15;
        LocalDate currentDate = LocalDate.parse("2023-04-14");
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId, new ArrayList<>());
        OrderAssignResponse expected = new OrderAssignResponse(currentDate, List.of(couriersGroupOrders));

        when(groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, currentDate)).thenReturn(new ArrayList<>());

        OrderAssignResponse actual = mainService.getCouriersAssignments(courierId, currentDate);

        assertEquals(expected, actual);
        verify(groupOrdersRepository).findAllByCourierIdEqualsAndDateEquals(courierId, currentDate);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersAssignmentsWhenCourierIdSetAndDateIsNullAndOrdersExistExecutedCorrectly() {
        long courierId = 11;
        LocalDate currentDate = LocalDate.now();

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(currentDate);

        List<OrderDto> orders1 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setGroupOrders(groupOrders1);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1.setOrders(orders1);

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setDate(currentDate);

        List<OrderDto> orders2 = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setGroupOrders(groupOrders2);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2.setOrders(orders2);

        List<GroupOrders> groupOrdersList = Arrays.asList(groupOrders1, groupOrders2);

        List<CouriersGroupOrders> courierGroupOrders = List.of(new CouriersGroupOrders(courierId, groupOrdersList));

        OrderAssignResponse expected = new OrderAssignResponse(currentDate, courierGroupOrders);

        when(groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, currentDate)).thenReturn(groupOrdersList);

        OrderAssignResponse actual = mainService.getCouriersAssignments(courierId, null);

        assertEquals(expected, actual);
        verify(groupOrdersRepository).findAllByCourierIdEqualsAndDateEquals(courierId, currentDate);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersAssignmentsWhenCourierIdIsNullAndDateIsNullAndOrdersExistExecutedCorrectly() {
        long courierId1 = 25;
        long courierId2 = 38;
        LocalDate currentDate = LocalDate.now();

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto1.setRegions(List.of(new Region(3)));
        courierDto1.setId(courierId1);

        GroupOrders groupOrders1_1 = new GroupOrders();
        groupOrders1_1.setDate(currentDate);
        groupOrders1_1.setCourierDto(courierDto1);

        List<OrderDto> orders1_1 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1_1.setOrders(orders1_1);

        GroupOrders groupOrders1_2 = new GroupOrders();
        groupOrders1_2.setDate(currentDate);
        groupOrders1_2.setCourierDto(courierDto1);

        List<OrderDto> orders1_2 = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1_2.setOrders(orders1_2);

        List<GroupOrders> groupOrdersList1 = Arrays.asList(groupOrders1_1, groupOrders1_2);

        CouriersGroupOrders courierGroupOrders1 = new CouriersGroupOrders(courierId1, groupOrdersList1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courierDto2.setRegions(List.of(new Region(5)));
        courierDto2.setId(courierId2);

        GroupOrders groupOrders2_1 = new GroupOrders();
        groupOrders2_1.setDate(currentDate);
        groupOrders2_1.setCourierDto(courierDto2);

        List<OrderDto> orders2_1 = IntStream.rangeClosed(49, 72).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2_1.setOrders(orders2_1);

        GroupOrders groupOrders2_2 = new GroupOrders();
        groupOrders2_2.setDate(currentDate);
        groupOrders2_2.setCourierDto(courierDto2);

        List<OrderDto> orders2_2 = IntStream.rangeClosed(73, 96).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2_2.setOrders(orders2_2);

        List<GroupOrders> groupOrdersList2 = Arrays.asList(groupOrders2_1, groupOrders2_2);

        CouriersGroupOrders courierGroupOrders2 = new CouriersGroupOrders(courierId2, groupOrdersList2);

        OrderAssignResponse expected =
                new OrderAssignResponse(currentDate, List.of(courierGroupOrders1, courierGroupOrders2));

        List<GroupOrders> groupOrdersListAll = new ArrayList<>();
        groupOrdersListAll.addAll(groupOrdersList1);
        groupOrdersListAll.addAll(groupOrdersList2);
        Collections.sort(groupOrdersListAll, (o1, o2) -> (int) (o1.getId() - o2.getId()));

        when(groupOrdersRepository.findAllByDateEquals(currentDate)).thenReturn(groupOrdersListAll);

        OrderAssignResponse actual = mainService.getCouriersAssignments(null, null);

        assertEquals(expected, actual);
        verify(groupOrdersRepository).findAllByDateEquals(currentDate);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void getCouriersAssignmentsWhenCourierIdIsNullAndDateSetAndOrdersExistExecutedCorrectly() {
        long courierId1 = 25;
        long courierId2 = 38;
        LocalDate currentDate = LocalDate.parse("2023-04-24");

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto1.setRegions(List.of(new Region(3)));
        courierDto1.setId(courierId1);

        GroupOrders groupOrders1_1 = new GroupOrders();
        groupOrders1_1.setDate(currentDate);
        groupOrders1_1.setCourierDto(courierDto1);

        List<OrderDto> orders1_1 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1_1.setOrders(orders1_1);

        GroupOrders groupOrders1_2 = new GroupOrders();
        groupOrders1_2.setDate(currentDate);
        groupOrders1_2.setCourierDto(courierDto1);

        List<OrderDto> orders1_2 = IntStream.rangeClosed(25, 48).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders1_2.setOrders(orders1_2);

        List<GroupOrders> groupOrdersList1 = Arrays.asList(groupOrders1_1, groupOrders1_2);

        CouriersGroupOrders courierGroupOrders1 = new CouriersGroupOrders(courierId1, groupOrdersList1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courierDto2.setRegions(List.of(new Region(5)));
        courierDto2.setId(courierId2);

        GroupOrders groupOrders2_1 = new GroupOrders();
        groupOrders2_1.setDate(currentDate);
        groupOrders2_1.setCourierDto(courierDto2);

        List<OrderDto> orders2_1 = IntStream.rangeClosed(49, 72).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2_1.setOrders(orders2_1);

        GroupOrders groupOrders2_2 = new GroupOrders();
        groupOrders2_2.setDate(currentDate);
        groupOrders2_2.setCourierDto(courierDto2);

        List<OrderDto> orders2_2 = IntStream.rangeClosed(73, 96).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            return orderDto;
        }).collect(Collectors.toList());
        groupOrders2_2.setOrders(orders2_2);

        List<GroupOrders> groupOrdersList2 = Arrays.asList(groupOrders2_1, groupOrders2_2);

        CouriersGroupOrders courierGroupOrders2 = new CouriersGroupOrders(courierId2, groupOrdersList2);

        OrderAssignResponse orderAssignResponse = new OrderAssignResponse(currentDate,
                List.of(courierGroupOrders1, courierGroupOrders2));

        OrderAssignResponse expected = orderAssignResponse;

        List<GroupOrders> groupOrdersListAll = new ArrayList<>();
        groupOrdersListAll.addAll(groupOrdersList1);
        groupOrdersListAll.addAll(groupOrdersList2);
        Collections.sort(groupOrdersListAll, (o1, o2) -> (int) (o1.getId() - o2.getId()));

        when(groupOrdersRepository.findAllByDateEquals(currentDate)).thenReturn(groupOrdersListAll);

        OrderAssignResponse actual = mainService.getCouriersAssignments(null, currentDate);

        assertEquals(expected, actual);
        verify(groupOrdersRepository).findAllByDateEquals(currentDate);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void assignOrdersWhenDateIsNullAndNoOrdersExistsExecutedCorrectly() {
        List<OrderDto> orders = new ArrayList<>();
        List<CourierDto> couriers = new ArrayList<>();
        OrderAssignResponse expected = new OrderAssignResponse(LocalDate.now(), new ArrayList<>());

        when(orderRepository.findAllByCompletedTimeIsNull()).thenReturn(orders);
        when(courierRepository.findAll(0, Integer.MAX_VALUE)).thenReturn(couriers);
        when(groupOrdersRepository.saveAllAndFlush(any(List.class))).thenReturn(new ArrayList());

        OrderAssignResponse actual = mainService.orderAssign(null);

        assertEquals(expected, actual);
        verify(orderRepository).findAllByCompletedTimeIsNull();
        verify(courierRepository).findAll(0, Integer.MAX_VALUE);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void assignOrdersWhenDateIsSetAndNoOrdersExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.parse("2023-04-25");
        List<OrderDto> orders = new ArrayList<>();
        List<CourierDto> couriers = new ArrayList<>();
        OrderAssignResponse expected = new OrderAssignResponse(currentDate, new ArrayList<>());

        when(orderRepository.findAllByCompletedTimeIsNull()).thenReturn(orders);
        when(courierRepository.findAll(0, Integer.MAX_VALUE)).thenReturn(couriers);

        OrderAssignResponse actual = mainService.orderAssign(currentDate);

        assertEquals(expected, actual);
        verify(orderRepository).findAllByCompletedTimeIsNull();
        verify(courierRepository).findAll(0, Integer.MAX_VALUE);
        verifyAllMocksNoMoreInteractions();
    }

    @Test
    public void assignOrdersWhenDateIsSetAndOrdersExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.parse("2023-04-25");
        List<OrderDto> orders = new ArrayList<>();

        long courierId1 = 5;
        Region region1 = new Region(1);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(courierId1);
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto1.setRating(10);
        courierDto1.setEarnings(1150);

        GroupOrders groupOrders1 = new GroupOrders(currentDate, courierDto1);

        List<OrderDto> orders1 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region1);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders1);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders1.setOrders(orders1);

        CouriersGroupOrders couriersGroupOrders1 = new CouriersGroupOrders(courierId1, List.of(groupOrders1));

        long courierId2 = 7;

        Region region2 = new Region(11);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setId(courierId2);
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto2.setRegions(Arrays.asList(region1, region2));
        courierDto2.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto2.setRating(9);
        courierDto2.setEarnings(100);

        GroupOrders groupOrders2 = new GroupOrders(currentDate, courierDto2);

        List<OrderDto> orders2 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region2);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders2);
            return orderDto;
        }).collect(Collectors.toList());

        orders.addAll(orders1);
        orders.addAll(orders2);

        groupOrders2.setOrders(orders2);

        CouriersGroupOrders couriersGroupOrders2 = new CouriersGroupOrders(courierId2, List.of(groupOrders2));

        OrderAssignResponse orderAssignResponse = new OrderAssignResponse(
                currentDate, List.of(couriersGroupOrders1, couriersGroupOrders2));

        OrderAssignResponse expected = orderAssignResponse;

        when(orderRepository.findAllByCompletedTimeIsNull()).thenReturn(orders);
        when(courierRepository.findAll(0, Integer.MAX_VALUE)).thenReturn(List.of(courierDto1, courierDto2));
        when(vrpService.solve(currentDate, List.of(courierDto1, courierDto2), orders)).thenReturn(expected);
        when(groupOrdersRepository.saveAllAndFlush(any(List.class))).thenReturn(List.of(groupOrders1, groupOrders2));

        OrderAssignResponse actual = mainService.orderAssign(currentDate);

        assertEquals(expected, actual);
        verify(orderRepository).findAllByCompletedTimeIsNull();
        verify(courierRepository).findAll(0, Integer.MAX_VALUE);
        verify(vrpService).solve(currentDate, List.of(courierDto1, courierDto2), orders);
        verify(groupOrdersRepository).saveAllAndFlush(any(List.class));
        verifyAllMocksNoMoreInteractions();
    }

    private void verifyAllMocksNoMoreInteractions() {
        verifyNoMoreInteractions(vrpService,
                courierRepository,
                regionRepository,
                orderRepository,
                groupOrdersRepository);
    }
}
