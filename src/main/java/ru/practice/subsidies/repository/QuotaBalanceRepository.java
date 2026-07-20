package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practice.subsidies.entity.QuotaBalance;

import java.util.Optional;

public interface QuotaBalanceRepository extends JpaRepository<QuotaBalance, Integer> {

    @Query("""
        SELECT qb FROM QuotaBalance qb
        WHERE qb.resident.documentType = :documentType
          AND qb.resident.documentNumber = :documentNumber
          AND qb.year = :year
    """)
    Optional<QuotaBalance> findByResidentDocumentAndYear(
            @Param("documentType") String documentType,
            @Param("documentNumber") String documentNumber,
            @Param("year") Integer year
    );
}
