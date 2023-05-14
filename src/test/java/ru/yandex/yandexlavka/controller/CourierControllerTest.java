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
import ru.yandex.yandexlavka.model.dto.CouriersGroupOrders;
import ru.yandex.yandexlavka.model.dto.CreateCourierDto;
import ru.yandex.yandexlavka.model.dto.CreateCourierRequest;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.service.MainService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

@WebMvcTest(CourierController.class)
@AutoConfigureMockMvc
public class CourierControllerTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MainService mainService;

    @Test
    public void createCouriersWhenOneCourierInRequestReturnOk() throws Exception {
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
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto = new CreateCourierDto();
        createCourierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto.setRegions(new HashSet<>(regionIds));
        createCourierDto.setWorkingHours(Set.of("09:00-12:00", "15:00-20:00"));
        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> regions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto = createCourierDto.toCourierDto(regions);
        courierDto.setId(1);
        List<CourierDto> response = Arrays.asList(courierDto);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createCouriers(any(CreateCourierRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateCourierRequest> captor = ArgumentCaptor.forClass(CreateCourierRequest.class);
        verify(mainService).createCouriers(captor.capture());
        assertEquals(createCourierRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createCouriersWhenWorkingHoursRepeatableReturnOk() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["15:00-20:00","09:00-12:00","15:00-20:00","15:00-20:00","09:00-12:00"]
                    }
                  ]
                }
                """;
        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto = new CreateCourierDto();
        createCourierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto.setRegions(new HashSet<>(regionIds));
        createCourierDto.setWorkingHours(Set.of("09:00-12:00", "15:00-20:00"));
        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> regions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto = createCourierDto.toCourierDto(regions);
        courierDto.setId(1);
        List<CourierDto> response = Arrays.asList(courierDto);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createCouriers(any(CreateCourierRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateCourierRequest> captor = ArgumentCaptor.forClass(CreateCourierRequest.class);
        verify(mainService).createCouriers(captor.capture());
        assertEquals(createCourierRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createCouriersWhenTwoCouriersInRequestReturnOk() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["19:00-20:00", "09:00-12:00","09:00-11:00"]
                    },
                    {
                      "courier_type":"BIKE",
                      "regions": [1,2,3],
                      "working_hours":["09:00-21:00"]
                    }                    
                  ]
                }
                """;

        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto1 = new CreateCourierDto();
        createCourierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto1.setRegions(new HashSet<>(regionIds));
        createCourierDto1.setWorkingHours(Set.of("19:00-20:00", "09:00-12:00", "09:00-11:00"));

        CreateCourierDto createCourierDto2 = new CreateCourierDto();
        createCourierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        createCourierDto2.setRegions(new HashSet<>(regionIds));
        createCourierDto2.setWorkingHours(Set.of("09:00-21:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto1, createCourierDto2));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> regions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto1 = createCourierDto1.toCourierDto(regions);
        courierDto1.setId(1);
        CourierDto courierDto2 = createCourierDto2.toCourierDto(regions);
        courierDto2.setId(2);
        List<CourierDto> response = Arrays.asList(courierDto1, courierDto2);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createCouriers(any(CreateCourierRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateCourierRequest> captor = ArgumentCaptor.forClass(CreateCourierRequest.class);
        verify(mainService).createCouriers(captor.capture());
        assertEquals(createCourierRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createCouriersWhenRegionsRepeatedReturnOk() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [1,2,3,1,2,3],
                      "working_hours":[ "19:00-21:00"]
                    }                 
                  ]
                }
                """;

        List<Integer> regionIds = Arrays.asList(1, 2, 3);
        CreateCourierDto createCourierDto1 = new CreateCourierDto();
        createCourierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        createCourierDto1.setRegions(new HashSet<>(regionIds));
        createCourierDto1.setWorkingHours(Set.of("19:00-21:00"));

        CreateCourierRequest createCourierRequest = new CreateCourierRequest();
        createCourierRequest.setCouriers(Arrays.asList(createCourierDto1));

        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> regions = Arrays.asList(region1, region2, region3);

        CourierDto courierDto1 = createCourierDto1.toCourierDto(regions);
        courierDto1.setId(1);
        List<CourierDto> response = Arrays.asList(courierDto1);

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.createCouriers(any(CreateCourierRequest.class))).thenReturn(response);
        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));
        ArgumentCaptor<CreateCourierRequest> captor = ArgumentCaptor.forClass(CreateCourierRequest.class);
        verify(mainService).createCouriers(captor.capture());
        assertEquals(createCourierRequest, captor.getValue());
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void createCouriersWhenNegativeRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [-1, 2 ],
                      "working_hours":["09:00-23:00"]
                    }
                  ]
                }
                """;
        String jsonResponse = """
                    {
                        "couriers[0].regions[]": "must be between 1 and 2147483647"
                    }
                """;

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));

        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenLongRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [2147483648],
                      "working_hours":["09:00-24:00"]
                    }
                  ]
                }
                """;
        String response = "JSON parse error: Numeric value (2147483648) out of range of int (-2147483648 - 2147483647)";

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenStringRegionReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": ["Region"],
                      "working_hours":["09:00-24:00"]
                    }
                  ]
                }
                """;
        String response = "JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String " +
                "\"Region\": not a valid `java.lang.Integer` value";

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenRegionsAreEmptyReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [],
                      "working_hours":["09:00-24:00"]
                    }
                  ]
                }
                """;
        String jsonResponse = """
                {
                    "couriers[0].regions": "must not be empty"                                
                }
                """;

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenCourierTypeIsInvalidReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"BACK",
                      "regions": [1,2,3],
                      "working_hours":["09:00-24:00"]
                    }
                  ]
                }
                """;
        String response =
                ".CourierDto$CourierTypeEnum` from String \"BACK\": not one of the values accepted for Enum class: " +
                        "[BIKE, FOOT, AUTO]";

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(response)));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenWorkingHoursAreInvalidReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["09:00-24:00"]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":[]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["12:00-25:00"]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["2:00-20:00"]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["12:00--23:00"]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":["22:00-05:00"]
                    }                                                           
                  ]
                }
                """;
        String jsonResponse = """
                    {
                        "couriers[5].workingHours[]":"Invalid time window interval",
                        "couriers[1].workingHours": "must not be empty",
                        "couriers[2].workingHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "couriers[4].workingHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "couriers[0].workingHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\"",
                        "couriers[3].workingHours[]": "must match \\"(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]-(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\\""
                                          
                    }
                """;

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenWorkingHoursAndRegionsAreEmptyReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"FOOT",
                      "regions": [],
                      "working_hours":["09:00-24:00"]
                    },
                     {
                      "courier_type":"FOOT",
                      "regions": [1,2,3],
                      "working_hours":[]
                    }
                  ]
                }
                """;
        String jsonResponse = """
                {
                    "couriers[0].regions": "must not be empty",
                    "couriers[1].workingHours": "must not be empty"
                }
                """;

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void createCouriersWhenBodyIsNullReturnBadRequest() throws Exception {
        String jsonRequest = "{}";
        String jsonResponse = """
                {
                    "couriers": "must not be null"  
                }
                """;

        this.mockMvc.perform(post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));
        verifyNoInteractions(mainService);
    }

    @Test
    public void getCouriersReturnOk() throws Exception {
        int offset = 0;
        int limit = 2;
        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(1);
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setId(2);
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(new Region(1), new Region(3)));
        courierDto2.setWorkingHours(List.of("08:00-21:00"));

        List<CourierDto> response = new ArrayList<>(Arrays.asList(courierDto1, courierDto2));

        String jsonResponse = objectMapper.writeValueAsString(response);

        when(mainService.getCouriers(offset, limit)).thenReturn(response);
        mockMvc.perform(get(format("/couriers?offset=%d&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].courier_id", is(1)))
                .andExpect(jsonPath("$[1].courier_id", is(2)));

        verify(mainService).getCouriers(offset, limit);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getCouriersWhenOffsetAndLimitIsNegativeReturnBadRequest() throws Exception {
        int offset = -10;
        int limit = -2;
        String jsonResponse = """
                    {
                        "offset":"must be between 0 and 2147483647",
                        "limit":"must be between 1 and 2147483647"
                    }     
                """;

        mockMvc.perform(get(format("/couriers?offset=%d&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCouriersWhenOffsetIsMoreThanIntMaxReturnBadRequest() throws Exception {
        String offset = "9223372036854775807";
        int limit = 2;
        String jsonResponse = """
                {
                    "offset": "Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \\"9223372036854775807\\""                                                }
                """;

        mockMvc.perform(get(format("/couriers?offset=%s&limit=%d", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCouriersWhenLimitIsMoreThanIntMaxReturneuest() throws Exception {
        int offset = 2;
        String limit = "9223372036854775807";
        String jsonResponse = """
                {
                    "limit": "Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \\"9223372036854775807\\""                                                }
                """;

        mockMvc.perform(get(format("/couriers?offset=%d&limit=%s", offset, limit)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(jsonResponse));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCourierByIdReturnOk() throws Exception {
        long courierId = 5;
        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        String jsonResponse = objectMapper.writeValueAsString(courierDto);

        when(mainService.getCourierById(courierId)).thenReturn(courierDto);
        mockMvc.perform(get("/couriers/" + courierId))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
        verify(mainService).getCourierById(courierId);

        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getCourierByIdWhenIdNotExistsReturnBadRequest() throws Exception {
        long courierId = 100;
        when(mainService.getCourierById(courierId)).thenThrow(new CourierNotFoundException(courierId));
        mockMvc.perform(get("/couriers/" + courierId))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(content().string("Could not find courier with id " + courierId))
                .andExpect(status().isBadRequest());
        verify(mainService).getCourierById(courierId);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getCourierByIdWhenMoreThanLongMaxReturnBadRequest() throws Exception {
        String id = "92233720368547758071";
        String outputMin = "Failed to convert value of type 'java.lang.String' to required type 'long'";

        mockMvc.perform(get("/couriers/" + id))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.courier_id", 0).value(containsString(outputMin)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoReturnOk() throws Exception {
        long courierId = 5;
        LocalDate startDate = LocalDate.parse("2023-04-20");
        LocalDate endDate = LocalDate.parse("2023-04-30");

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto.setRating(10);
        courierDto.setEarnings(1150);

        String jsonResponse = objectMapper.writeValueAsString(courierDto);

        when(mainService.getCourierMetaInfo(courierId, startDate, endDate)).thenReturn(courierDto);
        mockMvc.perform(get(format("/couriers/meta-info/%d?start_date=%s&end_date=%s", courierId, startDate, endDate)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(mainService).getCourierMetaInfo(courierId, startDate, endDate);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoWhenCourierNotExistsReturnBadRequest() throws Exception {
        long courierId = 5;
        LocalDate startDate = LocalDate.parse("2023-04-20");
        LocalDate endDate = LocalDate.parse("2023-04-30");

        when(mainService.getCourierMetaInfo(courierId, startDate, endDate)).thenThrow(new CourierNotFoundException(courierId));
        mockMvc.perform(get(format("/couriers/meta-info/%d?start_date=%s&end_date=%s", courierId, startDate, endDate)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andExpect(content().string("Could not find courier with id " + courierId))
                .andExpect(status().isBadRequest());

        verify(mainService).getCourierMetaInfo(courierId, startDate, endDate);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoWhenStartDateIsInvalidReturnBadRequest() throws Exception {
        long courierId = 5;
        LocalDate endDate = LocalDate.parse("2023-04-30");

        String response = "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'";

        mockMvc.perform(get(format("/couriers/meta-info/%d?start_date=2024-05-32&end_date=%s", courierId, endDate)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.start_date", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoWhenEndDateIsInvalidReturnBadRequest() throws Exception {
        long courierId = 5;
        LocalDate startDate = LocalDate.parse("2023-04-20");

        String response = "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'";

        mockMvc.perform(get(format("/couriers/meta-info/%d?start_date=%s&end_date=2024-13-25", courierId, startDate)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.end_date", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoWhenStartDateAndEndDateIsInvalidReturnBadRequest() throws Exception {
        long courierId = 5;

        String response = "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'";

        mockMvc.perform(get(format("/couriers/meta-info/%d?start_date=2023-05-33&end_date=2024-13-25", courierId)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.start_date", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void getCourierMetaInfoWhenCourierIdIsMoreMaxLongReturnBadRequest() throws Exception {
        String response = "Failed to convert value of type 'java.lang.String' to required type 'long'";

        mockMvc.perform(get(
                        "/couriers/meta-info/92233720368547758071?start_date=2023-05-15&end_date=2024-11-25"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.courier_id", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdIsMoreMaxLongReturnBadRequest() throws Exception {
        String response = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'";

        mockMvc.perform(get(
                        "/couriers/assignments?courier_id=92233720368547758071?"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.courier_id", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenDateIsInvalidReturnBadRequest() throws Exception {

        String response = "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'";

        mockMvc.perform(get("/couriers/assignments?date=2023-05-33"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date", containsString(response)));

        verifyNoInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdSetAndDateSetAndOrdersExistReturnOk() throws Exception {
        long courierId = 5;
        LocalDate date = LocalDate.parse("2023-04-20");

        Region region1 = new Region(1);

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto.setRating(10);
        courierDto.setEarnings(1150);

        GroupOrders groupOrders = new GroupOrders();
        groupOrders.setDate(date);
        groupOrders.setCourierDto(courierDto);

        List<OrderDto> orders = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region1);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders.setOrders(orders);

        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId, List.of(groupOrders));

        OrderAssignResponse orderAssignResponse = new OrderAssignResponse(date, List.of(couriersGroupOrders));

        String jsonResponse = objectMapper.writeValueAsString(orderAssignResponse);

        when(mainService.getCouriersAssignments(courierId, date)).thenReturn(orderAssignResponse);
        mockMvc.perform(get(format("/couriers/assignments?courier_id=%d&date=%s", courierId, date)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(mainService).getCouriersAssignments(courierId, date);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdSetAndDateIsNullAndOrdersExistReturnOk() throws Exception {
        long courierId = 5;
        LocalDate date = LocalDate.parse("2023-04-20");

        Region region1 = new Region(1);

        CourierDto courierDto = new CourierDto();
        courierDto.setId(courierId);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto.setRating(10);
        courierDto.setEarnings(1150);

        GroupOrders groupOrders = new GroupOrders();
        groupOrders.setDate(date);
        groupOrders.setCourierDto(courierDto);

        List<OrderDto> orders = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region1);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders.setOrders(orders);
        CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId, List.of(groupOrders));
        OrderAssignResponse orderAssignResponse = new OrderAssignResponse(date, List.of(couriersGroupOrders));

        String jsonResponse = objectMapper.writeValueAsString(orderAssignResponse);

        when(mainService.getCouriersAssignments(courierId, null)).thenReturn(orderAssignResponse);
        mockMvc.perform(get(format("/couriers/assignments?courier_id=%d", courierId)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(mainService).getCouriersAssignments(courierId, null);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdIsNullAndDateIsNullAndOrdersExistReturnOk() throws Exception {
        LocalDate date = LocalDate.parse("2023-04-20");

        long courierId1 = 5;
        Region region1 = new Region(1);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(courierId1);
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto1.setRating(10);
        courierDto1.setEarnings(1150);

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(date);
        groupOrders1.setCourierDto(courierDto1);

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

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setDate(date);
        groupOrders2.setCourierDto(courierDto2);

        List<OrderDto> orders2 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region2);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders2);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders2.setOrders(orders2);

        CouriersGroupOrders couriersGroupOrders2 = new CouriersGroupOrders(courierId2, List.of(groupOrders2));

        OrderAssignResponse orderAssignResponse1 = new OrderAssignResponse(date,
                List.of(couriersGroupOrders1, couriersGroupOrders2));


        String jsonResponse = objectMapper.writeValueAsString(orderAssignResponse1);

        when(mainService.getCouriersAssignments(null, null)).thenReturn(orderAssignResponse1);
        mockMvc.perform(get("/couriers/assignments"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
        verify(mainService).getCouriersAssignments(null, null);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdIsNullAndDateSetAndOrdersExistReturnOk() throws Exception {
        LocalDate date = LocalDate.parse("2023-04-20");

        long courierId1 = 5;
        Region region1 = new Region(1);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(courierId1);
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(new Region(1), new Region(2)));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierDto1.setRating(10);
        courierDto1.setEarnings(1150);

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(date);
        groupOrders1.setCourierDto(courierDto1);

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

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setDate(date);
        groupOrders2.setCourierDto(courierDto2);

        List<OrderDto> orders2 = IntStream.rangeClosed(1, 24).mapToObj(ind -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCost(ind);
            orderDto.setRegion(region2);
            orderDto.setCompletedTime(OffsetDateTime.parse("2023-03-20T01:00:00.000+00"));
            orderDto.setGroupOrders(groupOrders2);
            return orderDto;
        }).collect(Collectors.toList());

        groupOrders2.setOrders(orders2);

        CouriersGroupOrders couriersGroupOrders2 = new CouriersGroupOrders(courierId2, List.of(groupOrders2));

        OrderAssignResponse orderAssignResponse1 = new OrderAssignResponse(date,
                List.of(couriersGroupOrders1, couriersGroupOrders2));

        String jsonResponse = objectMapper.writeValueAsString(orderAssignResponse1);

        when(mainService.getCouriersAssignments(null, date)).thenReturn(orderAssignResponse1);
        mockMvc.perform(get(format("/couriers/assignments?date=%s", date)))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
        verify(mainService).getCouriersAssignments(null, date);
        verifyNoMoreInteractions(mainService);
    }

    @Test
    public void couriersAssignmentsWhenCourierIdIsNullAndDateIsNullAndOrdersNotExistReturnOk() throws Exception {
        LocalDate date = LocalDate.parse("2023-04-20");

        OrderAssignResponse orderAssignResponse1 = new OrderAssignResponse(date, new ArrayList<>());

        String jsonResponse = objectMapper.writeValueAsString(orderAssignResponse1);

        when(mainService.getCouriersAssignments(null, null)).thenReturn(orderAssignResponse1);
        mockMvc.perform(get("/couriers/assignments"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(mainService).getCouriersAssignments(null, null);
        verifyNoMoreInteractions(mainService);
    }

}
