package ru.practice.subsidies.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record FlightDto(
        Integer id,
        String flightNumber,
        String departureLocation,
        String arrivalLocation,
        LocalDate flightDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        Integer totalSeats,
        Integer availableSeats,
        Integer distanceKm,
        Integer maxPriceFedKopecks
) {
}
