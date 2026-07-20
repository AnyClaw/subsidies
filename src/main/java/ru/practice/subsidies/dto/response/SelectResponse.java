package ru.practice.subsidies.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record SelectResponse(
        ConfirmationInfoDto confirmationInfo,
        QuotaBalanceDto quotaBalance
) {
    public static SelectResponse generateRefutedResponse(String reason) {
        return SelectResponse.builder()
                .confirmationInfo(
                        ConfirmationInfoDto.builder()
                                .status("REFUTED")
                                .reason(reason)
                                .build()
                )
                .build();
    }
}
