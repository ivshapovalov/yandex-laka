package ru.yandex.yandexlavka.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.dto.CouriersGroupOrders;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.service.jsprit.JSpritVrpServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class JSpritVrpServiceTest extends CommonTest {

    @Autowired
    @InjectMocks
    private JSpritVrpServiceImpl vrpService;

    @MockBean
    private MainService mainService;

    @Test
    public void orderAssignWhenZeroCouriersAndOneOrderReturnEmptyResponse() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region2 = new Region(15);

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(10);
        order1.setRegion(region2);

        assertThrows(ConstraintViolationException.class, () -> {
            vrpService.solve(currentDate, new ArrayList<>(), List.of(order1));
        });
    }

    @Test
    public void orderAssignWhenOneCouriersAndZeroOrdersThrowsException() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region3 = new Region(33);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region3));

        assertThrows(ConstraintViolationException.class, () -> {
            vrpService.solve(currentDate, List.of(courier1), new ArrayList<>());
        });
    }

    @Test
    public void orderAssignWhenOneFootCourierInFirstRegionAndOneOrderInSecondRegionReturnEmptyResponse() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region2 = new Region(15);
        Region region3 = new Region(33);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(10);
        order1.setRegion(region2);

        OrderAssignResponse expected = new OrderAssignResponse(currentDate, new ArrayList<>());

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1));

        assertEquals(expected, actual);
        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(0, actual.getCouriersGroupOrdersList().size());
    }

    @Test
    public void orderAssignWhenOneFootCourierAndOneOrderInOneRegionAndWorkingTimeAreDifferentReturnEmpty() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(18);
        Region region2 = new Region(28);
        Region region3 = new Region(43);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("14:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(10);
        order1.setRegion(region1);

        OrderAssignResponse expected = new OrderAssignResponse(currentDate, new ArrayList<>());

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1));

        assertEquals(expected, actual);
        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(0, actual.getCouriersGroupOrdersList().size());
    }

    @Test
    public void orderAssignWhenOneFootCourierAndOneOrderInOneRegionWeightIsMoreThanCourierMaxWeightReturnEmpty() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(1);
        Region region2 = new Region(22);
        Region region3 = new Region(38);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("01:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(100);
        order1.setRegion(region1);

        OrderAssignResponse expected = new OrderAssignResponse(currentDate, new ArrayList<>());

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1));

        assertEquals(expected, actual);
        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(0, actual.getCouriersGroupOrdersList().size());
    }

    @Test
    public void orderAssignWhenOneFootCourierAndOneOrderInHisRegionReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(11);
        Region region2 = new Region(45);
        Region region3 = new Region(68);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(10);
        order1.setRegion(region1);

        GroupOrders groupOrders = new GroupOrders(currentDate, courier1);
        groupOrders.addOrder(order1);
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId1, List.of(groupOrders));
        OrderAssignResponse expected = new OrderAssignResponse(currentDate, List.of(couriersGroupOrders));

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1));

        assertEquals(expected, actual);
        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneFootCourierAndTwoOrdersInOneRegionWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(5);
        order1.setRegion(region3);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-13:00"));
        order2.setWeight(5);
        order2.setRegion(region3);

        GroupOrders groupOrders = new GroupOrders(currentDate, courier1);
        groupOrders.addOrder(order1);
        groupOrders.addOrder(order2);
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId1, List.of(groupOrders));
        OrderAssignResponse expected = new OrderAssignResponse(currentDate, List.of(couriersGroupOrders));

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 10, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneFootCourierAndTwoOrdersInOneRegionWithGroupingInMultiTimeWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(5);
        order1.setRegion(region3);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("18:00-21:00"));
        order2.setWeight(5);
        order2.setRegion(region3);

        GroupOrders groupOrders = new GroupOrders(currentDate, courier1);
        groupOrders.addOrder(order1);
        groupOrders.addOrder(order2);
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId1, List.of(groupOrders));
        OrderAssignResponse expected = new OrderAssignResponse(currentDate, List.of(couriersGroupOrders));

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2));

        assertEquals(expected, actual);
        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(18, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndTwoOrdersInTwoRegionsWithoutGroupingReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(9);
        Region region2 = new Region(18);
        Region region3 = new Region(27);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(5);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-21:00"));
        order2.setWeight(5);
        order2.setRegion(region2);

        GroupOrders groupOrders1 = new GroupOrders(currentDate, courier1);
        groupOrders1.addOrder(order1);
        GroupOrders groupOrders2 = new GroupOrders(currentDate, courier1);
        groupOrders2.addOrder(order2);
        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 25, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndTwoOrdersInOneRegionsAndOrderWeightMoreCourierMaxWeightWithoutGroupingReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(9);
        Region region2 = new Region(18);
        Region region3 = new Region(27);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(6);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-21:00"));
        order2.setWeight(6);
        order2.setRegion(region1);

        GroupOrders groupOrders1 = new GroupOrders(currentDate, courier1);
        groupOrders1.addOrder(order1);
        GroupOrders groupOrders2 = new GroupOrders(currentDate, courier1);
        groupOrders2.addOrder(order2);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 25, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndThreeOrdersInOneRegionsWithGroupingReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(9);
        Region region2 = new Region(18);
        Region region3 = new Region(27);
        long courierId1 = 18;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-15:00"));
        order1.setWeight(2);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-21:00"));
        order2.setWeight(2);
        order2.setRegion(region1);

        OrderDto order3 = new OrderDto();
        order3.setId(15);
        order3.setDeliveryHours(List.of("12:00-21:00"));
        order3.setWeight(4);
        order3.setRegion(region1);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2, order3));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 25, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 35, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(1).getAssignedTime());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndThreeOrdersInOneRegionWithGroupingReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(9);
        Region region2 = new Region(18);
        Region region3 = new Region(27);
        long courierId1 = 18;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("09:00-11:00"));
        order1.setWeight(2);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("14:00-15:00"));
        order2.setWeight(2);
        order2.setRegion(region1);

        OrderDto order3 = new OrderDto();
        order3.setId(15);
        order3.setDeliveryHours(List.of("18:00-21:00"));
        order3.setWeight(4);
        order3.setRegion(region1);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2, order3));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size()
                + actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndThreeOrdersInTwoRegionsWithoutGroupingReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region2 = new Region(215);
        Region region3 = new Region(38);

        long courierId1 = 18;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("00:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(1);
        order1.setDeliveryHours(List.of("09:00-11:00"));
        order1.setWeight(2);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(2);
        order2.setDeliveryHours(List.of("14:00-15:00"));
        order2.setWeight(4);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(3);
        order3.setDeliveryHours(List.of("18:00-21:00"));
        order3.setWeight(2);
        order3.setRegion(region1);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2, order3));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(2).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 00, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(14, 00, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(18, 00, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(2).getOrders().get(0).getAssignedTime());
    }

    @Test
    public void orderAssignResponseWhenOneFootCourierAndTwoConcurrentOrdersInTwoRegionsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region2 = new Region(215);
        Region region3 = new Region(38);

        long courierId1 = 18;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courier1.setWorkingHours(List.of("00:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(1);
        order1.setDeliveryHours(List.of("09:00-09:10"));
        order1.setWeight(2);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(2);
        order2.setDeliveryHours(List.of("09:00-09:10"));
        order2.setWeight(4);
        order2.setRegion(region2);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1), List.of(order1, order2));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 00, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());

    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFourOrdersInOneRegionWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(5);
        order1.setRegion(region3);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-13:00"));
        order2.setWeight(5);
        order2.setRegion(region3);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("12:00-13:00"));
        order3.setWeight(5);
        order3.setRegion(region3);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("12:00-13:00"));
        order4.setWeight(5);
        order4.setRegion(region3);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(4, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 8, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 16, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(2).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 24, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(3).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFourOrdersInTwoRegionsWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(5);
        order1.setRegion(region2);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-13:00"));
        order2.setWeight(5);
        order2.setRegion(region3);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("12:00-13:00"));
        order3.setWeight(5);
        order3.setRegion(region2);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("12:00-13:00"));
        order4.setWeight(5);
        order4.setRegion(region3);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(4, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFourOrdersInOneRegionWhenTotalWeightMoreThanCourierMaxWeightWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(10);
        order1.setRegion(region2);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-13:00"));
        order2.setWeight(5);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("12:00-13:00"));
        order3.setWeight(4);
        order3.setRegion(region2);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("12:00-13:00"));
        order4.setWeight(5);
        order4.setRegion(region2);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 12, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 20, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(1).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 28, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(2).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFiveOrdersInOneRegionWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-13:00"));
        order1.setWeight(1);
        order1.setRegion(region2);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-13:00"));
        order2.setWeight(2);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("12:00-13:00"));
        order3.setWeight(4);
        order3.setRegion(region2);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("12:00-13:00"));
        order4.setWeight(5);
        order4.setRegion(region2);

        OrderDto order5 = new OrderDto();
        order5.setId(27);
        order5.setDeliveryHours(List.of("12:00-13:00"));
        order5.setWeight(2);
        order5.setRegion(region2);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4, order5));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(5,
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size()
                        + actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFiveOrdersInFiveRegionsWithGroupingInOneTimeWindowReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        Region region4 = new Region(24);
        Region region5 = new Region(28);

        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-23:59"));
        courier1.setRegions(List.of(region1, region2, region3, region4, region5));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("12:00-14:00"));
        order1.setWeight(1);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("12:00-14:00"));
        order2.setWeight(2);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("12:00-14:00"));
        order3.setWeight(4);
        order3.setRegion(region3);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("12:00-14:00"));
        order4.setWeight(5);
        order4.setRegion(region4);

        OrderDto order5 = new OrderDto();
        order5.setId(27);
        order5.setDeliveryHours(List.of("12:00-14:00"));
        order5.setWeight(2);
        order5.setRegion(region5);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4, order5));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(2).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 12, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 24, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 36, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().get(1).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(12, 48, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(2).getOrders().get(0).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneBikeCourierAndFiveOrdersInOneRegionsWithGroupingInTwoTimeWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        Region region4 = new Region(24);
        Region region5 = new Region(28);

        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-09:30", "12:00-12:30"));
        courier1.setRegions(List.of(region1, region2, region3, region4, region5));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order1.setWeight(1);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("00:00-23:59"));
        order2.setWeight(2);
        order2.setRegion(region1);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("00:00-23:59"));
        order3.setWeight(4);
        order3.setRegion(region1);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("00:00-23:59"));
        order4.setWeight(5);
        order4.setRegion(region1);

        OrderDto order5 = new OrderDto();
        order5.setId(27);
        order5.setDeliveryHours(List.of("00:00-23:59"));
        order5.setWeight(2);
        order5.setRegion(region1);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4, order5));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(5,
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size()
                        + actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
    }

    @Test
    public void orderAssignWhenTwoBikeCouriersAndFourOrdersInFourRegionsWithGroupingInOneWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        Region region4 = new Region(24);
        Region region5 = new Region(28);

        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier1.setWorkingHours(List.of("09:00-09:30"));
        courier1.setRegions(List.of(region1, region2, region3, region4, region5));

        long courierId2 = 157;
        CourierDto courier2 = new CourierDto();
        courier2.setId(courierId2);
        courier2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courier2.setWorkingHours(List.of("09:00-09:30"));
        courier2.setRegions(List.of(region1, region2, region3, region4, region5));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order1.setWeight(1);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order2.setDeliveryHours(List.of("00:00-23:59"));
        order2.setWeight(2);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order3.setDeliveryHours(List.of("00:00-23:59"));
        order3.setWeight(4);
        order3.setRegion(region4);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("00:00-23:59"));
        order4.setWeight(5);
        order4.setRegion(region3);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1, courier2),
                List.of(order1, order2, order3, order4));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(2, actual.getCouriersGroupOrdersList().size());
        assertEquals(courierId2, actual.getCouriersGroupOrdersList().get(0).getCourierId());
        assertEquals(courierId1, actual.getCouriersGroupOrdersList().get(1).getCourierId());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertEquals(1, actual.getCouriersGroupOrdersList().get(1).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(1).getGroupOrders().get(0).getOrders().size());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 12, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 24, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 12, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(1).getGroupOrders().get(0).getOrders().get(0).getAssignedTime());
        assertEquals(OffsetDateTime.of(currentDate, LocalTime.of(9, 24, 0, 0), ZoneOffset.UTC),
                actual.getCouriersGroupOrdersList().get(1).getGroupOrders().get(0).getOrders().get(1).getAssignedTime());
    }

    @Test
    public void orderAssignWhenOneAutoCourierAndEightOrdersInOneRegionWithGroupingInOneWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(3);
        Region region2 = new Region(57);
        Region region3 = new Region(22);
        Region region4 = new Region(24);
        Region region5 = new Region(28);

        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courier1.setWorkingHours(List.of("09:00-18:00"));
        courier1.setRegions(List.of(region1, region2, region3, region4, region5));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order1.setWeight(1);
        order1.setRegion(region3);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order2.setWeight(2);
        order2.setRegion(region3);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order3.setWeight(4);
        order3.setRegion(region3);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("00:00-23:59"));
        order4.setWeight(5);
        order4.setRegion(region3);

        OrderDto order5 = new OrderDto();
        order5.setId(33);
        order5.setDeliveryHours(List.of("00:00-23:59"));
        order5.setWeight(5);
        order5.setRegion(region3);

        OrderDto order6 = new OrderDto();
        order6.setId(34);
        order6.setDeliveryHours(List.of("00:00-23:59"));
        order6.setWeight(4);
        order6.setRegion(region3);

        OrderDto order7 = new OrderDto();
        order7.setId(45);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order7.setWeight(3);
        order7.setRegion(region3);

        OrderDto order8 = new OrderDto();
        order8.setId(46);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order8.setWeight(4);
        order8.setRegion(region3);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4, order5, order6, order7, order8));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
    }

    @Test
    public void orderAssignWhenOneAutoCourierAndEightOrdersInEightRegionsWithGroupingInOneWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region2 = new Region(11);
        Region region3 = new Region(12);
        Region region4 = new Region(13);
        Region region5 = new Region(14);
        Region region6 = new Region(15);
        Region region7 = new Region(16);
        Region region8 = new Region(17);

        long courierId1 = 15;
        CourierDto courier1 = new CourierDto();
        courier1.setId(courierId1);
        courier1.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courier1.setWorkingHours(List.of("09:00-18:00"));
        courier1.setRegions(List.of(region1, region2, region3, region4, region5, region6, region7, region8));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order1.setWeight(1);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order2.setWeight(2);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order3.setWeight(4);
        order3.setRegion(region3);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("00:00-23:59"));
        order4.setWeight(5);
        order4.setRegion(region4);

        OrderDto order5 = new OrderDto();
        order5.setId(33);
        order5.setDeliveryHours(List.of("00:00-23:59"));
        order5.setWeight(5);
        order5.setRegion(region5);

        OrderDto order6 = new OrderDto();
        order6.setId(34);
        order6.setDeliveryHours(List.of("00:00-23:59"));
        order6.setWeight(4);
        order6.setRegion(region6);

        OrderDto order7 = new OrderDto();
        order7.setId(45);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order7.setWeight(3);
        order7.setRegion(region7);

        OrderDto order8 = new OrderDto();
        order8.setId(46);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order8.setWeight(4);
        order8.setRegion(region8);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courier1),
                List.of(order1, order2, order3, order4, order5, order6, order7, order8));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(1, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(0).getOrders().size());
        assertEquals(3, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(1).getOrders().size());
        assertEquals(2, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().get(2).getOrders().size());
    }

    @Test
    public void orderAssignWhenFootAndBikeAndAutoCouriersAndEightOrdersInEightRegionsWithGroupingInOneWindowsReturnOk() {
        LocalDate currentDate = LocalDate.parse("2023-05-08");

        Region region1 = new Region(10);
        Region region2 = new Region(11);
        Region region3 = new Region(12);
        Region region4 = new Region(13);
        Region region5 = new Region(14);
        Region region6 = new Region(15);
        Region region7 = new Region(16);
        Region region8 = new Region(17);

        long courierFootId = 15;
        CourierDto courierFoot = new CourierDto();
        courierFoot.setId(courierFootId);
        courierFoot.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierFoot.setWorkingHours(List.of("09:00-18:00"));
        courierFoot.setRegions(List.of(region1, region2, region3));

        long courierBikeId = 16;
        CourierDto courierBike = new CourierDto();
        courierBike.setId(courierBikeId);
        courierBike.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierBike.setWorkingHours(List.of("09:00-18:00"));
        courierBike.setRegions(List.of(region3, region4, region5));

        long courierAutoId = 17;
        CourierDto courierAuto = new CourierDto();
        courierAuto.setId(courierAutoId);
        courierAuto.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courierAuto.setWorkingHours(List.of("09:00-18:00"));
        courierAuto.setRegions(List.of(region6, region7, region8));

        OrderDto order1 = new OrderDto();
        order1.setId(11);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order1.setWeight(1);
        order1.setRegion(region1);

        OrderDto order2 = new OrderDto();
        order2.setId(13);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order2.setWeight(2);
        order2.setRegion(region2);

        OrderDto order3 = new OrderDto();
        order3.setId(25);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order3.setWeight(4);
        order3.setRegion(region3);

        OrderDto order4 = new OrderDto();
        order4.setId(18);
        order4.setDeliveryHours(List.of("00:00-23:59"));
        order4.setWeight(5);
        order4.setRegion(region4);

        OrderDto order5 = new OrderDto();
        order5.setId(33);
        order5.setDeliveryHours(List.of("00:00-23:59"));
        order5.setWeight(5);
        order5.setRegion(region5);

        OrderDto order6 = new OrderDto();
        order6.setId(34);
        order6.setDeliveryHours(List.of("00:00-23:59"));
        order6.setWeight(4);
        order6.setRegion(region6);

        OrderDto order7 = new OrderDto();
        order7.setId(45);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order7.setWeight(3);
        order7.setRegion(region7);

        OrderDto order8 = new OrderDto();
        order8.setId(46);
        order1.setDeliveryHours(List.of("00:00-23:59"));
        order8.setWeight(4);
        order8.setRegion(region8);

        OrderAssignResponse actual = vrpService.solve(currentDate, List.of(courierFoot, courierBike, courierAuto),
                List.of(order1, order2, order3, order4, order5, order6, order7, order8));

        assertNotNull(actual.getCouriersGroupOrdersList());
        assertEquals(3, actual.getCouriersGroupOrdersList().size());
        assertNotNull(actual.getCouriersGroupOrdersList().get(0).getGroupOrders());
        assertEquals(courierBikeId, actual.getCouriersGroupOrdersList().get(0).getCourierId());
        assertEquals(courierAutoId, actual.getCouriersGroupOrdersList().get(1).getCourierId());
        assertEquals(courierFootId, actual.getCouriersGroupOrdersList().get(2).getCourierId());
        assertEquals(5, actual.getCouriersGroupOrdersList().get(0).getGroupOrders().size()
                + actual.getCouriersGroupOrdersList().get(1).getGroupOrders().size()
                + actual.getCouriersGroupOrdersList().get(2).getGroupOrders().size()
        );

    }
}
