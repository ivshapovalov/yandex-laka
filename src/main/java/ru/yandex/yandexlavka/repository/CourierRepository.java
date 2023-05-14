package ru.yandex.yandexlavka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.yandexlavka.model.entity.CourierDto;

import java.util.List;

@Repository
public interface CourierRepository extends JpaRepository<CourierDto, Long> {
    @Query(value = "Select o.* from couriers o order by o.id limit ?2 offset ?1 ", nativeQuery = true)
    List<CourierDto> findAll(int offset, int limit);
}
