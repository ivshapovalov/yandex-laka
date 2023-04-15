package ru.yandex.yandexlavka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.exceptions.CourierNotFoundException;
import ru.yandex.yandexlavka.exceptions.CourierOrderNotFoundException;
import ru.yandex.yandexlavka.exceptions.OrderAlreadyCompletedException;
import ru.yandex.yandexlavka.exceptions.OrderNotFoundException;
import ru.yandex.yandexlavka.model.dto.CompleteOrderDto;
import ru.yandex.yandexlavka.model.dto.CompleteOrderRequest;
import ru.yandex.yandexlavka.model.dto.CreateOrderDto;
import ru.yandex.yandexlavka.model.dto.CreateOrderRequest;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.service.MainService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
public class OrderControllerTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MainService mainService;

    @Test
    public void createOrdersWhenOneOrderInRequestReturnOk() throws Exception {
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
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setRegion(1);
        createOrderDto.setCost(100);
        createOrderDto.setWeight(100.0f);
        createOrderDto.setDeliveryHours(Set.of("09:00-18:00", "19:00-21:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto));

        List<OrderDto> response = Arrays.asList(createOrderDto.toOrderDto(new Region(1)));

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createOrders(any(CreateOrderRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(mainService).createOrders(captor.capture());
        assertEquals(createOrderRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createOrdersWhenOneOrderWithRepeatedDeliveryHoursInRequestReturnOk() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 100.0,
                          "region": 1,
                          "delivery_hours":["19:00-21:00","09:00-18:00", "19:00-21:00","09:00-18:00", "19:00-21:00","09:00-18:00"],
                          "cost": 100
                        }
                      ]
                    }
                """;
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setRegion(1);
        createOrderDto.setCost(100);
        createOrderDto.setWeight(100.0f);
        createOrderDto.setDeliveryHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto));

        List<OrderDto> response = Arrays.asList(createOrderDto.toOrderDto(new Region(1)));

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createOrders(any(CreateOrderRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(mainService).createOrders(captor.capture());
        assertEquals(createOrderRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createOrdersWhenTwoOrdersInRequestReturnOk() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 100.0,
                          "region": 1,
                          "delivery_hours":[ "19:00-21:00","09:00-18:00"],
                          "cost": 100
                        },
                         {
                          "weight": 200.0,
                          "region": 2,
                          "delivery_hours":["19:00-21:00","15:00-16:00"],
                          "cost": 200
                        }
                      ]
                    }
                """;
        CreateOrderDto createOrderDto1 = new CreateOrderDto();
        createOrderDto1.setRegion(1);
        createOrderDto1.setCost(100);
        createOrderDto1.setWeight(100.0f);
        createOrderDto1.setDeliveryHours(Set.of("19:00-21:00", "09:00-18:00"));

        CreateOrderDto createOrderDto2 = new CreateOrderDto();
        createOrderDto2.setRegion(2);
        createOrderDto2.setCost(200);
        createOrderDto2.setWeight(200.0f);
        createOrderDto2.setDeliveryHours(Set.of("19:00-21:00", "15:00-16:00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrders(Arrays.asList(createOrderDto1, createOrderDto2));

        List<OrderDto> response = Arrays.asList(
                createOrderDto1.toOrderDto(new Region(1)), createOrderDto2.toOrderDto(new Region(2)));
        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createOrders(any(CreateOrderRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(mainService).createOrders(captor.capture());
        assertEquals(createOrderRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createOrdersWhenNegativeRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 5.5,
                          "region": -1,
                          "delivery_hours":["09:00-12:00","15:00-22:00"],
                          "cost": 10
                        }
                      ]
                    }
                """;
        String jsonResponse = """
                {
                    "orders[0].region": "must be between 1 and 2147483647"
                }
                """;

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenLongRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 5.5,
                          "region": 2147483648,
                          "delivery_hours":["09:00-12:00","15:00-22:00"],
                          "cost": 10
                        }
                      ]
                    }
                """;
        String response = "JSON parse error: Numeric value (2147483648) out of range of int (-2147483648 - 2147483647)";

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenStringRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 5.8,
                          "region": "Region",
                          "delivery_hours":["09:00-12:00","15:00-22:00"],
                          "cost": 10
                        }
                      ]
                    }
                """;
        String response =
                "JSON parse error: Cannot deserialize value of type `int` from String \"Region\": not a valid `int` value";

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenRegionIsEmptyReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight":"5.5",
                          "region": "",
                          "delivery_hours":["09:00-12:00","15:00-22:00"],
                          "cost":"10"
                        }
                      ]
                    }
                """;
        String jsonResponse = """
                {
                    "orders[0].region": "must be between 1 and 2147483647"                                
                }
                """;

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenDeliveryHoursAreInvalidReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "orders": [
                       {
                            "weight":5.5,
                            "region": 1,
                            "delivery_hours":[],
                            "cost":"10"
                       },
                       {
                            "weight":5.5,
                            "region": 1,
                            "delivery_hours":["09:00-24:00"],
                            "cost": 10
                       },
                       {
                            "weight":5.5,
                            "region": 1,
                            "delivery_hours":["09:00-24:00"],
                            "cost":10
                       },
                       {
                            "weight":5.5,
                            "region": 1,
                            "delivery_hours":["9:00-23:00"],
                            "cost":10
                       },
                       {
                            "weight":5.5,
                            "region": 1,
                            "delivery_hours":["09:00--23:00"],
                            "cost":10
                       }                               
                  ]
                }
                """;
        String jsonResponse = """
                    {
                        "orders[4].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[1].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[2].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[3].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[0].deliveryHours": "must not be empty"
                    }
                """;

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenDeliveryHoursAndRegionAreInvalidReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "orders": [
                        {
                          "weight": 5.5,
                          "region": -1,
                          "delivery_hours":["09:00-24:00"],
                          "cost": 10
                        },
                            {
                          "weight": 7,
                          "region": 1,
                          "delivery_hours":["9:00-24:00"],
                          "cost": 10
                        }
                      ]
                    }
                """;
        String jsonResponse = """
                    {
                        "orders[0].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[1].deliveryHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "orders[0].region": "must be between 1 and 2147483647"
                    }
                """;

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createOrdersWhenBodyIsNullReturnBadRequest() throws Exception {
        String jsonRequest = "{}";
        String jsonResponse = """
                {
                    "orders": "must not be null"  
                }
                """;

        this.mockMvc.perform(post("/orders")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void getOrdersReturnOk() throws Exception {
        int offset = 0;
        int limit = 2;
        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(1);
        orderDto1.setWeight(15.1f);
        orderDto1.setCost(15);
        orderDto1.setRegion(new Region(1));
        orderDto1.setDeliveryHours(List.of("09:00-18:00", "19:00-21:00"));

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setId(2);
        orderDto2.setWeight(5.1f);
        orderDto2.setCost(1);
        orderDto2.setRegion(new Region(2));
        orderDto2.setDeliveryHours(List.of("19:00-21:00"));

        List<OrderDto> response = new ArrayList<>(Arrays.asList(orderDto1, orderDto2));
        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.getOrders(offset, limit)).thenReturn(response);
        mockMvc.perform(get(format("/orders?offset=%d&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].order_id", is(1)))
                .andExpect(jsonPath("$[1].order_id", is(2)));
        verify(mainService).getOrders(offset, limit);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getOrdersWhenOffsetAndLimitIsNegativeReturnBadRequest() throws Exception {
        int offset = -10;
        int limit = -2;
        String jsonResponse = """                
                    {
                        "offset":"must be between 0 and 2147483647",
                        "limit":"must be between 1 and 2147483647"
                    }                   
                """;

        mockMvc.perform(get(format("/orders?offset=%d&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void getOrdersWhenOffsetIsMoreThanIntMaxReturnBadRequest() throws Exception {
        String offset = "9223372036854775807";
        int limit = 2;
        String jsonResponse = """
                {
                    "offset": "Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \\"9223372036854775807\\""                                                }
                """;

        mockMvc.perform(get(format("/orders?offset=%s&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void getOrdersWhenLimitIsMoreThanIntMaxReturnBadRequest() throws Exception {
        int offset = 2;
        String limit = "9223372036854775807";
        String jsonResponse = """
                {
                    "limit": "Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \\"9223372036854775807\\""                                                }
                """;

        mockMvc.perform(get(format("/orders?offset=%d&limit=%s", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void getOrderByIdReturnOk() throws Exception {
        long orderId = 5;
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setCost(100);
        orderDto.setWeight(100.0f);
        orderDto.setRegion(new Region(1));
        orderDto.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));

        String jsonResponse = objectMapper.writeValueAsString(orderDto);

        when(mainService.getOrderById(orderId)).thenReturn(orderDto);
        mockMvc.perform(get("/orders/" + orderId))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
        verify(mainService).getOrderById(orderId);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getOrderByIdWhenIdNotExistsReturnBadRequest() throws Exception {
        long orderId = 100;
        when(mainService.getOrderById(orderId)).thenThrow(new OrderNotFoundException(orderId));
        mockMvc.perform(get("/orders/" + orderId))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(content().string("Could not find order with id " + orderId))
                .andExpect(status().isBadRequest());
        verify(mainService).getOrderById(orderId);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getOrderByIdWhenMoreThanLongMaxReturnBadRequest() throws Exception {
        String orderId = "92233720368547758071";
        String output = "Failed to convert value of type 'java.lang.String' to required type 'long'";

        mockMvc.perform(get("/orders/" + orderId))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.order_id", 0).value(containsString(output)));
        verifyNoInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOneOrderInRequestReturnOk() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.000+00:00"
                        }
                      ]
                    }
                """;

        long orderId = 1;
        CompleteOrderDto completeOrderDto = new CompleteOrderDto();
        completeOrderDto.setCourierId(1);
        completeOrderDto.setOrderId(orderId);
        completeOrderDto.setCompleteTime(OffsetDateTime.parse("2023-05-23T04:56:07.000+00:00"));

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(orderId);
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0f);
        orderDto1.setRegion(new Region(1));
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderDto1.setCompletedTime(OffsetDateTime.now());

        courierDto1.setGroupOrders(List.of(new GroupOrders()));

        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(Arrays.asList(completeOrderDto));

        List<OrderDto> response = Arrays.asList(orderDto1);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertEquals(completeOrderRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenTwoOrdersInRequestReturnOk() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.000+00:00"
                        },
                        {
                          "courier_id": 2,
                          "order_id": 2,
                          "complete_time":"2023-05-23T09:56:07.000+00:00"
                        }                        
                      ]
                    }
                """;

        long orderId1 = 1;
        long orderId2 = 2;
        CompleteOrderDto completeOrderDto1 = new CompleteOrderDto();
        completeOrderDto1.setCourierId(1);
        completeOrderDto1.setOrderId(orderId1);
        completeOrderDto1.setCompleteTime(OffsetDateTime.parse("2023-05-23T04:56:07.000+00:00"));

        CompleteOrderDto completeOrderDto2 = new CompleteOrderDto();
        completeOrderDto2.setCourierId(2);
        completeOrderDto2.setOrderId(orderId2);
        completeOrderDto2.setCompleteTime(OffsetDateTime.parse("2023-05-23T09:56:07.000+00:00"));

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(new Region(2)));
        courierDto2.setWorkingHours(List.of("19:00-21:00"));

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(orderId1);
        orderDto1.setCost(100);
        orderDto1.setWeight(100.0f);
        orderDto1.setRegion(new Region(1));
        orderDto1.setDeliveryHours(List.of("19:00-21:00", "09:00-18:00"));
        orderDto1.setCompletedTime(OffsetDateTime.now());

        courierDto1.setGroupOrders(List.of(new GroupOrders()));

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setId(orderId2);
        orderDto2.setCost(200);
        orderDto2.setWeight(2);
        orderDto2.setRegion(new Region(2));
        orderDto2.setDeliveryHours(List.of("09:00-18:00"));
        orderDto2.setCompletedTime(OffsetDateTime.now());

        courierDto2.setGroupOrders(List.of(new GroupOrders()));

        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        completeOrderRequest.setCompleteOrders(Arrays.asList(completeOrderDto1, completeOrderDto2));

        List<OrderDto> response = Arrays.asList(orderDto1, orderDto2);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertEquals(completeOrderRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOneOrderAndCourierNotExistsReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.0+00:00"
                        }
                      ]
                    }
                """;

        String response = "Could not find courier with id 1";

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenThrow(new CourierNotFoundException(1L));
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertFalse(captor.getValue().getCompleteOrders().isEmpty());
        assertEquals(1, captor.getValue().getCompleteOrders().size());
        CompleteOrderDto completeOrderDto = captor.getValue().getCompleteOrders().get(0);
        assertEquals(1, completeOrderDto.getCourierId());
        assertEquals(1, completeOrderDto.getOrderId());
        assertEquals(OffsetDateTime.parse("2023-05-23T04:56:07.0+00:00"), completeOrderDto.getCompleteTime());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenBodyIsNullReturnBadRequest() throws Exception {
        String jsonRequest = "{}";

        String jsonResponse = """
                    {
                        "completeOrders": "must not be empty"
                    }
                """;

        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOneOrderAndOrderNotExistsReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.0+00:00"
                        }
                      ]
                    }
                """;

        String response = "Could not find order with id 1";

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenThrow(new OrderNotFoundException(1L));
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertFalse(captor.getValue().getCompleteOrders().isEmpty());
        assertEquals(1, captor.getValue().getCompleteOrders().size());
        CompleteOrderDto completeOrderDto = captor.getValue().getCompleteOrders().get(0);
        assertEquals(1, completeOrderDto.getCourierId());
        assertEquals(1, completeOrderDto.getOrderId());
        assertEquals(OffsetDateTime.parse("2023-05-23T04:56:07.0+00:00"), completeOrderDto.getCompleteTime());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOneOrderAndCourierOrderNotExistsReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.0+00:00"
                        }
                      ]
                    }
                """;

        String response = "Could not find courier's 1 order 1";

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenThrow(new CourierOrderNotFoundException(1L, 1L));
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertFalse(captor.getValue().getCompleteOrders().isEmpty());
        assertEquals(1, captor.getValue().getCompleteOrders().size());
        CompleteOrderDto completeOrderDto = captor.getValue().getCompleteOrders().get(0);
        assertEquals(1, completeOrderDto.getCourierId());
        assertEquals(1, completeOrderDto.getOrderId());
        assertEquals(OffsetDateTime.parse("2023-05-23T04:56:07.0+00:00"), completeOrderDto.getCompleteTime());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOneOrderAlreadyCompletedReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56:07.0+00:00"
                        }
                      ]
                    }
                """;

        String response = "Order 1 already completed";

        when(mainService.completeOrder(any(CompleteOrderRequest.class))).thenThrow(new OrderAlreadyCompletedException(1L));
        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        ArgumentCaptor<CompleteOrderRequest> captor = ArgumentCaptor.forClass(CompleteOrderRequest.class);
        verify(mainService).completeOrder(captor.capture());
        assertFalse(captor.getValue().getCompleteOrders().isEmpty());
        assertEquals(1, captor.getValue().getCompleteOrders().size());
        CompleteOrderDto completeOrderDto = captor.getValue().getCompleteOrders().get(0);
        assertEquals(1, completeOrderDto.getCourierId());
        assertEquals(1, completeOrderDto.getOrderId());
        assertEquals(OffsetDateTime.parse("2023-05-23T04:56:07.0+00:00"), completeOrderDto.getCompleteTime());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenCourierIdMoreMaxLongReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id": 922337203685477580755 ,
                          "order_id": 1,
                          "complete_time":"2023-04-03 19:25"
                        }
                      ]
                    }
                """;

        String response = "JSON parse error: Numeric value (922337203685477580755) out of range of long (-9223372036854775808 - 9223372036854775807)";

        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenOrderIdMoreMaxLongReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id":  1,
                          "order_id": 922337203685477580755,
                          "complete_time":"2023-04-03 19:25"
                        }
                      ]
                    }
                """;

        String response = "JSON parse error: Numeric value (922337203685477580755) out of range of long (-9223372036854775808 - 9223372036854775807)";

        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void completeOrdersWhenCompleteTimeIsInvalidReturnBadRequest() throws Exception {
        String jsonRequest = """
                    {
                      "complete_info": [
                        {
                          "courier_id":  1,
                          "order_id": 1,
                          "complete_time":"2023-05-23T04:56"
                        }
                      ]
                    }
                """;

        String response = "JSON parse error: Cannot deserialize value";

        this.mockMvc.perform(post("/orders/complete")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(response)));
        verifyNoInteractions(mainService);
    }

}
