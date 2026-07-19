package ru.practice.subsidies.dto.request;

import java.util.List;

public record SelectRequest(
        List<UserDto> passengers,
        FlightInfoDto flightInfo
) {
}
