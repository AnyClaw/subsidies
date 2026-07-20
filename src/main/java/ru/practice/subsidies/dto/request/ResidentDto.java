package ru.practice.subsidies.dto.request;

import java.time.LocalDate;

public record ResidentDto(
        String surname,
        String name,
        String patronymic,
        LocalDate birthday,
        String documentType,
        String documentNumber
) {
}
