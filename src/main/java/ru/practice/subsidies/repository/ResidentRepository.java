package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practice.subsidies.dto.request.ResidentDto;
import ru.practice.subsidies.entity.Resident;

import java.util.Optional;

public interface ResidentRepository extends JpaRepository<Resident, Integer> {

    @Query("""
        SELECT r FROM Resident r
        WHERE r.surname = :#{#resident.surname}
        AND r.name = :#{#resident.name}
        AND r.patronymic = :#{#resident.patronymic}
        AND r.birthday = :#{#resident.birthday}
        AND r.documentType = :#{#resident.documentType}
        AND r.documentNumber = :#{#resident.documentNumber}
        """)
    Optional<Resident> findByResidentDto(@Param("resident") ResidentDto resident);
}
