package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.subsidies.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Integer> {
}
