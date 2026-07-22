package ru.practice.subsidies.dto.request;

import ru.practice.subsidies.enums.SubsidyProgram;

public record TicketDto(
        String number,
        Integer flightId,
        SubsidyProgram fareType,
        Integer totalPriceKopecks
) {
}
