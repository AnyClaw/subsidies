package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.subsidies.entity.Flight;

public interface FlightRepository extends JpaRepository<Flight, Integer> {
}
