package ru.yandex.yandexlavka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.entity.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegionRepositoryTest extends CommonTest {

    @Autowired
    private RegionRepository regionRepository;

    @Test
    public void findAllWhenNoOneRegionExistsExecutedCorrectly() {
        List<Region> regions = regionRepository.findAll();
        assertThat(regions).isEmpty();
    }

    @Test
    public void findAllWhenOneRegionExistsExecutedCorrectly() {
        Region region1 = new Region(1);
        regionRepository.saveAndFlush(region1);

        List<Region> regions = regionRepository.findAll();

        assertEquals(1, regions.size());
        assertEquals(region1, regions.get(0));
    }

    @Test
    public void findAllWhenThreeRegionsExistExecutedCorrectly() {
        Region region1 = new Region(1);
        Region region2 = new Region(2);
        Region region3 = new Region(3);
        List<Region> expected = new ArrayList<>(Arrays.asList(region1, region2, region3));
        regionRepository.saveAndFlush(region1);
        regionRepository.saveAndFlush(region2);
        regionRepository.saveAndFlush(region3);

        List<Region> actual = regionRepository.findAll();

        assertEquals(3, actual.size());
        assertIterableEquals(expected, actual);
    }
}
