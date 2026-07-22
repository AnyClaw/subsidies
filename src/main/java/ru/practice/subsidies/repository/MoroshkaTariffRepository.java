package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.subsidies.entity.MoroshkaTariff;

import java.util.Optional;

public interface MoroshkaTariffRepository extends JpaRepository<MoroshkaTariff, Integer> {
    Optional<MoroshkaTariff> findByRoute_Id(Integer routeId);
}
