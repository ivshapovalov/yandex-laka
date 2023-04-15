package ru.yandex.yandexlavka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.yandexlavka.model.entity.CourierDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CourierRepository extends JpaRepository<CourierDto, Long> {
    @Query(value = "Select o.* from couriers o order by o.id asc limit ?2 offset ?1 ", nativeQuery = true)
    List<CourierDto> findAll(int offset, int limit);


//    @Query(value =
//            """
//                select
//                    c.*,
//                    o.id as order_id
//                from (select c_sub.* from couriers c_sub where c_sub.id=?1) as c
//                left join courier_orders co on c.id=co.courier_id
//                left join (
//                    select o_sub.*
//                    from orders o_sub
//                    where o_sub.completed_time>=?2 and o_sub.completed_time<?3) as o on o.id=co.order_id
//                order by c.id,o.completed_time
//           """, nativeQuery = true)
//    List<CourierDto> findByIdWithCompletedOrdersBetweenDates(long courierId, OffsetDateTime startDate, OffsetDateTime endDate);

//    @Query(value =
//            """
//                select
//                    c
//                from CourierDto c
//                left join (select o from OrderDto o where o.completedTime>=?2 and o.completedTime<?3) o
//                where c.id=?1
//           """)
//    List<CourierDto> findByIdWithCompletedOrdersBetweenDatesJPQL(long courierId, OffsetDateTime startDate,
//                                                              OffsetDateTime endDate);


}
