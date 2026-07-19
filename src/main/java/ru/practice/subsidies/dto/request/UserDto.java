package ru.practice.subsidies.dto.request;

import java.time.LocalDate;

public record UserDto(
        String surname,
        String name,
        String patronymic,
        LocalDate birthday,
        String document         // 2007-12-03
) {
}
