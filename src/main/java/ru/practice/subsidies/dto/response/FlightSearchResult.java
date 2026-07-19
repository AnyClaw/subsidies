package ru.practice.subsidies.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record FlightSearchResult(
        Integer flightId,
        String flightNumber,
        LocalDate flightDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        Integer totalSeats,
        Integer availableSeats,
        String program,
        Boolean isBidirectional
) { }
