package ru.practice.subsidies.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record InsertResponse(
        Integer operationId,
        String status,
        String message,
        QuotaBalanceDto quotaBalance
) {
}
