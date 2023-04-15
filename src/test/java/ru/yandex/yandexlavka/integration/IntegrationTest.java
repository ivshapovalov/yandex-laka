package ru.yandex.yandexlavka.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.repository.CourierRepository;
import ru.yandex.yandexlavka.repository.OrderRepository;
import ru.yandex.yandexlavka.repository.RegionRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class IntegrationTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private OrderRepository orderRepository;

//    @AfterEach
//    void deleteEntities() {
//        courierRepository.deleteAll();
//        regionRepository.deleteAll();
//        orderRepository.deleteAll();
//    }

    @Test
    void createCouriersWhenOneCourierInRequestWorksThroughAllLayers() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"BIKE",
                      "regions": [3,1,2],
                      "working_hours":["15:00-20:00","09:00-12:00"]
                    }
                  ]
                }
                """;
        CourierDto courierDto = new CourierDto();
        courierDto.setId(1);
        courierDto.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto.setRegions(List.of(new Region(1), new Region(2), new Region(3)));
        courierDto.setWorkingHours(List.of("09:00-12:00", "15:00-20:00"));
        List<CourierDto> response = Arrays.asList(courierDto);

        String jsonResponse = objectMapper.writeValueAsString(response);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(jsonResponse));

        List<CourierDto> couriers = courierRepository.findAll(0, 1000);
        List<Region> regions = regionRepository.findAll();
        List<OrderDto> orders = orderRepository.findAll();
        assertEquals(0, orders.size());
        assertEquals(3, regions.size());
        assertEquals(1, couriers.size());
        assertEquals(courierDto, couriers.get(0));
    }

    @Test
    void createCouriersWhenTwoCouriersInRequestWorksThroughAllLayers() throws Exception {
        String jsonRequest = """
                {
                  "couriers": [
                    {
                      "courier_type":"BIKE",
                      "regions": [3,1,2],
                      "working_hours":["15:00-20:00","09:00-12:00"]
                    },
                     {
                      "courier_type":"AUTO",
                      "regions": [5,2,1],
                      "working_hours":["05:00-20:00","21:00-23:59"]
                    }
                  ]
                }
                """;
        CourierDto courierDto1 = new CourierDto();
        courierDto1.setId(1);
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto1.setRegions(List.of(new Region(1), new Region(2), new Region(3)));
        courierDto1.setWorkingHours(List.of("09:00-12:00", "15:00-20:00"));

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setId(2);
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.AUTO);
        courierDto2.setRegions(List.of(new Region(1), new Region(2), new Region(5)));
        courierDto2.setWorkingHours(List.of("05:00-20:00", "21:00-23:59"));

        List<CourierDto> response = Arrays.asList(courierDto1, courierDto2);

        String jsonResponse = objectMapper.writeValueAsString(response);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/couriers")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(jsonResponse));

        List<CourierDto> couriers = courierRepository.findAll(0, 1000);
        List<Region> regions = regionRepository.findAll();
        List<OrderDto> orders = orderRepository.findAll();
        assertEquals(0, orders.size());
        assertEquals(4, regions.size());
        assertEquals(2, couriers.size());
        assertIterableEquals(response, couriers);
    }

}

