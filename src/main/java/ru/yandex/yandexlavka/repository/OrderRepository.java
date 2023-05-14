package ru.yandex.yandexlavka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderDto, Long> {

    @Query(value = "Select o.* from orders o order by o.id limit ?2 offset ?1 ", nativeQuery = true)
    List<OrderDto> findAll(int offset, int limit);

    List<OrderDto> findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(List<GroupOrders> groupOrders,
                                                                                                  OffsetDateTime startDate, OffsetDateTime endDate);

    List<OrderDto> findAllByCompletedTimeIsNull();
}
