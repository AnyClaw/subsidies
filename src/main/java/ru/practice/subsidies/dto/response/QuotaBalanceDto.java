package ru.practice.subsidies.dto.response;

import lombok.Builder;

@Builder
public record QuotaBalanceDto(
        Integer year,
        Integer available,
        Integer issued,
        Integer refunded,
        Integer used
) {
}
