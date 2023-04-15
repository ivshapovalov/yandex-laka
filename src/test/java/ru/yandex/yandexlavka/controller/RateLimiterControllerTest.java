package ru.yandex.yandexlavka.controller;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.dto.CreateCourierRequest;
import ru.yandex.yandexlavka.model.dto.CreateOrderRequest;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.service.MainService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest
//@SpringBootTest(classes = {CourierController.class,OrderController.class,HealthController.class})
@SpringBootTest
@AutoConfigureMockMvc
//@ActiveProfiles("test-h2")
//@WebMvcTest(HealthController.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class RateLimiterControllerTest extends CommonTest {

    private static final int REQUEST_PER_PERIOD = 3;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MainService mainService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("resilience4j.ratelimiter.configs.default.limitForPeriod", () -> REQUEST_PER_PERIOD);
        registry.add("resilience4j.ratelimiter.configs.default.limitRefreshPeriod", () -> "10s");
        registry.add("resilience4j.ratelimiter.configs.default.timeoutDuration", () -> "100ms");
    }

    @RepeatedTest(10)
    public void healthControllerRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();
        mockMvc.perform(get("/ping"))
                .andExpect(result);
    }


    @RepeatedTest(10)
    public void courierControllerGetCourierByIdRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        long courierId = 5;
        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.getCourierById(courierId)).thenReturn(courierDto);
        mockMvc.perform(get("/couriers/" + courierId))
                .andDo(print())
                .andExpect(result);
    }

    @RepeatedTest(10)
    public void courierControllerGetCouriersRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.getCouriers(1, 1)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/couriers?offset=1&limit=1"))
                .andDo(print())
                .andExpect(result);
    }

    @RepeatedTest(10)
    public void courierControllerCreateCouriersRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["15:00-20:00","09:00-12:00"]
                    }
                  ]
                }
                """;
        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.createCouriers(new CreateCourierRequest())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result);
    }

    @RepeatedTest(10)
    public void orderControllerGetOrderByIdRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {

        long orderId = 5;
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setCost(100);
        orderDto.setWeight(100.0f);
        orderDto.setRegion(new Region(1));
        orderDto.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));

        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.getOrderById(orderId)).thenReturn(orderDto);
        mockMvc.perform(get("/orders/" + orderId))
                .andDo(print())
                .andExpect(result);
    }

    @RepeatedTest(10)
    public void orderControllerGetOrdersRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.getOrders(1, 1)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/orders?offset=1&limit=1"))
                .andDo(print())
                .andExpect(result);
    }

    @RepeatedTest(10)
    public void orderControllerCreateOrderRateLimiter(
            RepetitionInfo repetitionInfo) throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 100.0,
                          "region": 1,
                          "delivery_hours":["19:00-21:00","09:00-18:00"],
                          "cost": 100
                        }
                      ]
                    }
                """;
        ResultMatcher result = repetitionInfo.getCurrentRepetition()
                <= REQUEST_PER_PERIOD ? status().isOk() : status().isTooManyRequests();

        when(mainService.createOrders(new CreateOrderRequest())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result);
    }

}
