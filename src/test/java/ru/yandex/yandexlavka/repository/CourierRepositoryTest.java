package ru.yandex.yandexlavka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CourierRepositoryTest extends CommonTest {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    public void getCouriersWhenNoOneCourierExistsExecutedCorrectly() {
        int offset = 0;
        int limit = 1;
        List<CourierDto> couriers = courierRepository.findAll(offset, limit);
        assertThat(couriers).isEmpty();
    }

    @Test
    public void getCouriersWhenOneCourierExistsExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(region1, region2));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));

        courierRepository.saveAndFlush(courierDto1);

        int offset = 0;
        int limit = 1;
        List<CourierDto> couriers = courierRepository.findAll(offset, limit);
        assertEquals(1, couriers.size());
        assertEquals(courierDto1, couriers.get(0));
    }

    @Test
    public void getCouriersWhenThreeCouriersExistAndOffset1Limit2ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(region1));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierRepository.saveAndFlush(courierDto1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(region2));
        courierDto2.setWorkingHours(List.of("09:00-21:00"));
        courierRepository.saveAndFlush(courierDto2);

        CourierDto courierDto3 = new CourierDto();
        courierDto3.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto3.setRegions(Arrays.asList(region3));
        courierDto3.setWorkingHours(List.of("08:00-21:00"));
        courierRepository.saveAndFlush(courierDto3);

        List<CourierDto> expected = new ArrayList<>(Arrays.asList(courierDto2, courierDto3));

        int offset = 1;
        int limit = 2;
        List<CourierDto> actual = courierRepository.findAll(offset, limit);
        assertEquals(2, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getCouriersWhenThreeCouriersExistAndOffset0Limit2ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(region1));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierRepository.saveAndFlush(courierDto1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(region2));
        courierDto2.setWorkingHours(List.of("09:00-21:00"));
        courierRepository.saveAndFlush(courierDto2);

        CourierDto courierDto3 = new CourierDto();
        courierDto3.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto3.setRegions(Arrays.asList(region3));
        courierDto3.setWorkingHours(List.of("08:00-21:00"));
        courierRepository.saveAndFlush(courierDto3);

        int offset = 0;
        int limit = 2;
        List<CourierDto> actual = courierRepository.findAll(offset, limit);
        List<CourierDto> expected = new ArrayList<>(Arrays.asList(courierDto1, courierDto2));

        assertEquals(2, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getCouriersWhenThreeCouriersExistAndOffset2Limit3ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(region1));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierRepository.saveAndFlush(courierDto1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(region2));
        courierDto2.setWorkingHours(List.of("09:00-21:00"));
        courierRepository.saveAndFlush(courierDto2);

        CourierDto courierDto3 = new CourierDto();
        courierDto3.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto3.setRegions(Arrays.asList(region3));
        courierDto3.setWorkingHours(List.of("08:00-21:00"));
        courierRepository.saveAndFlush(courierDto3);

        List<CourierDto> expected = new ArrayList<>(Arrays.asList(courierDto3));

        int offset = 2;
        int limit = 3;
        List<CourierDto> actual = courierRepository.findAll(offset, limit);
        assertEquals(1, actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getCouriersWhenThreeCouriersExistAndOffset3Limit1ExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);

        CourierDto courierDto1 = new CourierDto();
        courierDto1.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        courierDto1.setRegions(Arrays.asList(region1));
        courierDto1.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierRepository.saveAndFlush(courierDto1);

        CourierDto courierDto2 = new CourierDto();
        courierDto2.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto2.setRegions(Arrays.asList(region2));
        courierDto2.setWorkingHours(List.of("09:00-21:00"));
        courierRepository.saveAndFlush(courierDto2);

        CourierDto courierDto3 = new CourierDto();
        courierDto3.setCourierType(CourierDto.CourierTypeEnum.BIKE);
        courierDto3.setRegions(Arrays.asList(region3));
        courierDto3.setWorkingHours(List.of("08:00-21:00"));
        courierRepository.saveAndFlush(courierDto3);

        int offset = 3;
        int limit = 1;
        List<CourierDto> actual = courierRepository.findAll(offset, limit);
        assertEquals(0, actual.size());
    }

    @Test
    public void getCourierByIdWhenCourierNotExistsExecutedCorrectly() {
        long id = 1;
        Optional<CourierDto> courier = courierRepository.findById(id);
        assertTrue(courier.isEmpty());
    }

    @Test
    public void getCourierByIdWhenCourierExistsExecutedCorrectly() throws InterruptedException {
        Region region1 = new Region(1);
        Region region2 = new Region(2);

        CourierDto expected = new CourierDto();
        expected.setCourierType(CourierDto.CourierTypeEnum.FOOT);
        expected.setRegions(Arrays.asList(region1, region2));
        expected.setWorkingHours(List.of("09:00-18:00", "19:00-21:00"));
        courierRepository.saveAndFlush(expected);

        long id = 1;
        Optional<CourierDto> actual = courierRepository.findById(id);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }
}
