package ru.yandex.yandexlavka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.yandexlavka.model.entity.GroupOrders;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GroupOrdersRepository extends JpaRepository<GroupOrders, Integer> {

    @Query(value = """
            Select g.* 
            from group_orders g
            where g.courier_id=?1 and date=?2
            order by g.courier_id , g.id 
            """, nativeQuery = true)
    List<GroupOrders> findAllByCourierIdEqualsAndDateEquals(long courierId, LocalDate date);

    List<GroupOrders> findAllByDateEquals(LocalDate date);

}
