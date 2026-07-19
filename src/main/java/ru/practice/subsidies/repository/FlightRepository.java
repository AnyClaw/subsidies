package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practice.subsidies.dto.response.FlightSearchResult;
import ru.practice.subsidies.entity.Flight;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
        SELECT
            f.id AS flightId,
            f.flightNumber AS flightNumber,
            f.flightDate AS flightDate,
            f.departureTime AS departureTime,
            f.arrivalTime AS arrivalTime,
            f.totalSeats AS totalSeats,
            f.availableSeats AS availableSeats,
            r.program AS program,
            r.isBidirectional AS isBidirectional
        FROM Flight f
        JOIN f.route r
        JOIN r.departureLocation dep
        JOIN r.arrivalLocation arr
        WHERE dep.code = :departureCode
          AND arr.code = :arrivalCode
          AND f.flightDate >= CURRENT_DATE
          AND f.availableSeats > 0
        ORDER BY f.flightDate ASC, f.departureTime ASC
    """)
    List<FlightSearchResult> findAvailableFlightsByRoute(
            @Param("departureCode") String departureCode,
            @Param("arrivalCode") String arrivalCode
    );
}
