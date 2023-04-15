package ru.yandex.yandexlavka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GroupOrdersRepositoryTest extends CommonTest {

    @Autowired
    private GroupOrdersRepository groupOrdersRepository;

    @Test
    public void findAllByDateEqualsWhenNoOneGroupExistsExecutedCorrectly() {
        LocalDate date = LocalDate.now();
        List<GroupOrders> groupOrders = groupOrdersRepository.findAllByDateEquals(date);
        assertThat(groupOrders).isEmpty();
    }

    @Test
    public void findAllByDateEqualsWhenOneGroupExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(currentDate);
        groupOrdersRepository.saveAndFlush(groupOrders1);

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByDateEquals(currentDate);
        assertEquals(1, groupOrdersList.size());
        assertEquals(groupOrders1, groupOrdersList.get(0));
    }

    @Test
    public void findAllByDateEqualsWhenThreeGroupsExistExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(currentDate);
        groupOrdersRepository.saveAndFlush(groupOrders1);

        GroupOrders groupOrders2 = new GroupOrders();
        groupOrders2.setDate(currentDate);
        groupOrdersRepository.saveAndFlush(groupOrders2);

        GroupOrders groupOrders3 = new GroupOrders();
        groupOrders3.setDate(currentDate);
        groupOrdersRepository.saveAndFlush(groupOrders3);

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByDateEquals(currentDate);
        assertEquals(3, groupOrdersList.size());
        assertIterableEquals(Arrays.asList(groupOrders1, groupOrders2, groupOrders3), groupOrdersList);
    }

    @Test
    public void findAllByIdEqualsAndDateEqualsWhenNoOneGroupExistsExecutedCorrectly() {
        long courierId = 1256;
        LocalDate date = LocalDate.now();
        List<GroupOrders> groupOrders = groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(courierId, date);
        assertThat(groupOrders).isEmpty();
    }

    @Test
    public void findAllByCourierIdEqualsAndDateEqualsWhenOneGroupExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        CourierDto courierDto = new CourierDto();

        GroupOrders groupOrders1 = new GroupOrders();
        groupOrders1.setDate(currentDate);
        groupOrders1.setCourierDto(courierDto);
        groupOrdersRepository.saveAndFlush(groupOrders1);

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(
                courierDto.getId(),
                currentDate);
        assertEquals(1, groupOrdersList.size());
        assertEquals(groupOrders1, groupOrdersList.get(0));
    }

    @Test
    public void findAllByCourierIdEqualsAndDateEqualsWhenSeveralGroupsExistExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        CourierDto courierDto1 = new CourierDto();
        CourierDto courierDto2 = new CourierDto();
        CourierDto courierDto3 = new CourierDto();

        GroupOrders groupOrdersCourier1_1 = new GroupOrders();
        groupOrdersCourier1_1.setDate(currentDate);
        groupOrdersCourier1_1.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier2_1 = new GroupOrders();
        groupOrdersCourier2_1.setDate(currentDate);
        groupOrdersCourier2_1.setCourierDto(courierDto2);

        GroupOrders groupOrdersCourier1_2 = new GroupOrders();
        groupOrdersCourier1_2.setDate(currentDate);
        groupOrdersCourier1_2.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier3_1 = new GroupOrders();
        groupOrdersCourier3_1.setDate(currentDate);
        groupOrdersCourier3_1.setCourierDto(courierDto3);

        groupOrdersRepository.saveAllAndFlush(
                List.of(groupOrdersCourier1_1, groupOrdersCourier1_2, groupOrdersCourier2_1, groupOrdersCourier3_1));

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(
                courierDto1.getId(),
                currentDate);
        assertEquals(2, groupOrdersList.size());
        assertIterableEquals(List.of(groupOrdersCourier1_1,groupOrdersCourier1_2), groupOrdersList);
    }

    @Test
    public void findAllByCourierIdEqualsAndDateEqualsWhenSeveralDatesExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        CourierDto courierDto1 = new CourierDto();

        GroupOrders groupOrdersCourier1_1 = new GroupOrders();
        groupOrdersCourier1_1.setDate(currentDate);
        groupOrdersCourier1_1.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_2 = new GroupOrders();
        groupOrdersCourier1_2.setDate(currentDate.plusDays(1));
        groupOrdersCourier1_2.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_3 = new GroupOrders();
        groupOrdersCourier1_3.setDate(currentDate.minusDays(1));
        groupOrdersCourier1_3.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_4 = new GroupOrders();
        groupOrdersCourier1_4.setDate(currentDate);
        groupOrdersCourier1_4.setCourierDto(courierDto1);

        groupOrdersRepository.saveAllAndFlush(
                List.of(groupOrdersCourier1_1, groupOrdersCourier1_3, groupOrdersCourier1_3, groupOrdersCourier1_4));

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(
                courierDto1.getId(),
                currentDate);
        assertEquals(2, groupOrdersList.size());
        assertIterableEquals(List.of(groupOrdersCourier1_1,groupOrdersCourier1_4), groupOrdersList);
    }

    @Test
    public void findAllByCourierIdEqualsAndDateEqualsWhenSeveralGroupsExistAndSeveralDatesExistsExecutedCorrectly() {
        LocalDate currentDate = LocalDate.now();

        CourierDto courierDto1 = new CourierDto();
        CourierDto courierDto2 = new CourierDto();
        CourierDto courierDto3 = new CourierDto();

        GroupOrders groupOrdersCourier1_1 = new GroupOrders();
        groupOrdersCourier1_1.setDate(currentDate);
        groupOrdersCourier1_1.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_2 = new GroupOrders();
        groupOrdersCourier1_2.setDate(currentDate.plusDays(1));
        groupOrdersCourier1_2.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_3 = new GroupOrders();
        groupOrdersCourier1_3.setDate(currentDate.minusDays(1));
        groupOrdersCourier1_3.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier1_4 = new GroupOrders();
        groupOrdersCourier1_4.setDate(currentDate);
        groupOrdersCourier1_4.setCourierDto(courierDto1);

        GroupOrders groupOrdersCourier2_1 = new GroupOrders();
        groupOrdersCourier2_1.setDate(currentDate);
        groupOrdersCourier2_1.setCourierDto(courierDto2);

        GroupOrders groupOrdersCourier3_1 = new GroupOrders();
        groupOrdersCourier3_1.setDate(currentDate);
        groupOrdersCourier3_1.setCourierDto(courierDto3);

        groupOrdersRepository.saveAllAndFlush(
                List.of(groupOrdersCourier1_1,
                        groupOrdersCourier1_3,
                        groupOrdersCourier1_3,
                        groupOrdersCourier1_4,
                        groupOrdersCourier2_1,
                        groupOrdersCourier3_1));

        List<GroupOrders> groupOrdersList = groupOrdersRepository.findAllByCourierIdEqualsAndDateEquals(
                courierDto1.getId(),
                currentDate);
        assertEquals(2, groupOrdersList.size());
        assertIterableEquals(List.of(groupOrdersCourier1_1,groupOrdersCourier1_4), groupOrdersList);
    }

}
