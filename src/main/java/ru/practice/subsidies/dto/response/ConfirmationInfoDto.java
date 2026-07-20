package ru.practice.subsidies.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import ru.practice.subsidies.enums.SubsidyProgram;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ConfirmationInfoDto(
        SubsidyProgram type,
        String status,
        Integer priceKopecks,
        String reason
) {
}
