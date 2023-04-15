package ru.yandex.yandexlavka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderDto, Long> {
    @Query(value = "Select o.* from orders o order by o.id asc limit ?2 offset ?1 ", nativeQuery = true)
    List<OrderDto> findAll(int offset, int limit);

    List<OrderDto> findAllByIdInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(List<Long> groupOrders,
                                                                                                  OffsetDateTime startDate, OffsetDateTime endDate);

    List<OrderDto> findAllByGroupOrdersInAndCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(List<GroupOrders> groupOrders,
                                                                                       OffsetDateTime startDate, OffsetDateTime endDate);

    List<OrderDto> findAllByCompletedTimeGreaterThanEqualAndCompletedTimeLessThan(OffsetDateTime startDate, OffsetDateTime endDate);

    @Query(value =
            """
                          select
                             sum(CASE
                                       WHEN c.courier_type=0 THEN 2
                                       WHEN c.courier_type=1 THEN 3
                                       WHEN c.courier_type=2 THEN 4
                                 END*o.cost
                             ) as earnings
                         from orders o
                         left join courier_orders co on o.id=co.order_id
                         left join couriers c on c.id=co.courier_id
                         where c.id=1
                         group by c.id         
                    """, nativeQuery = true)
    long calculateEarnings(long courierId, LocalDate startDate, LocalDate endDate);

    List<OrderDto> findAllByCompletedTimeIsNull();
}
