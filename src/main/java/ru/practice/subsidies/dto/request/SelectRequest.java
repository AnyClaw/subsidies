package ru.practice.subsidies.dto.request;

public record SelectRequest(
        ResidentDto residentInfo,
        Integer flightId
) { }
