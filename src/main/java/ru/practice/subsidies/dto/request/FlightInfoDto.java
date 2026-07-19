package ru.practice.subsidies.dto.request;

import java.time.LocalDate;

public record FlightInfoDto(
        String from,
        String to,
        LocalDate flightDate,   // 2007-12-03
        Boolean isBidirectional
) {
}
