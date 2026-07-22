package ru.practice.subsidies.dto.request;

import ru.practice.subsidies.enums.TicketStatus;

import java.time.LocalDateTime;

public record InsertRequest(
        TicketStatus operationType,
        LocalDateTime operationDateTime,
        TicketDto ticket,
        ResidentDto passenger
) {
}
