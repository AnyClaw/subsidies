package ru.practice.subsidies.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.ConfirmationInfoDto;
import ru.practice.subsidies.dto.response.QuotaBalanceDto;
import ru.practice.subsidies.dto.response.SelectResponse;
import ru.practice.subsidies.entity.Flight;
import ru.practice.subsidies.entity.QuotaBalance;
import ru.practice.subsidies.enums.SubsidyProgram;
import ru.practice.subsidies.repository.FlightRepository;
import ru.practice.subsidies.repository.QuotaBalanceRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubsidiesService {

    private final FlightRepository flightRepository;
    private final QuotaBalanceRepository quotaBalanceRepository;

    public SelectResponse select(SelectRequest selectRequest) {
        LocalDate currentDate = LocalDate.now();
        Optional<Flight> flightOp = flightRepository.findById(selectRequest.flightId());

        if (flightOp.isEmpty()) {
            String reason = "Рейс id = " + selectRequest.flightId() + " не найден!";
            return SelectResponse.generateRefutedResponse(reason);
        }

        Flight flight = flightOp.get();

        if (currentDate.isAfter(flight.getFlightDate())) {
            String reason = "Рейс id = " + flight.getId() + " уже начался!";
            return SelectResponse.generateRefutedResponse(reason);
        }

        if (flight.getAvailableSeats() == 0) {
            String reason = "На рейс id = " + flight.getId() + " не осталось свободных мест!";
            return SelectResponse.generateRefutedResponse(reason);
        }

        if (flight.getRoute().getProgram().name().equals(SubsidyProgram.FEDERAL.name())) {
            return SelectResponse.builder()
                    .confirmationInfo(
                            ConfirmationInfoDto.builder()
                                    .type(SubsidyProgram.FEDERAL)
                                    .status("CONFIRMED")
                                    .priceKopecks(flight.getRoute().getMaxPriceFedKopecks())
                                    .build()
                    )
                    .build();
        }

        Optional<QuotaBalance> quotaBalanceOp = quotaBalanceRepository.findByResidentDocumentAndYear(
                selectRequest.residentInfo().documentType(),
                selectRequest.residentInfo().documentNumber(),
                currentDate.getYear()
        );

        if (quotaBalanceOp.isEmpty() || !quotaBalanceOp.get().getResident().getHasCard()) {
            return SelectResponse.generateRefutedResponse("Вы не являетесь пользователем карты МОРОШКА");
        }

        QuotaBalance quotaBalance = quotaBalanceOp.get();

        int remaining = quotaBalance.getAvailable() - quotaBalance.getIssued() + quotaBalance.getRefunded();
        if (remaining <= 0) {
            return SelectResponse.generateRefutedResponse("Лимит исчерпан");
        }

        return SelectResponse.builder()
                .confirmationInfo(
                        ConfirmationInfoDto.builder()
                                .type(SubsidyProgram.MOROSHKA)
                                .status("CONFIRMED")
                                .priceKopecks(flight.getRoute().getMaxPriceFedKopecks())
                                .build()
                )
                .quotaBalance(
                        QuotaBalanceDto.builder()
                                .year(quotaBalance.getYear())
                                .available(quotaBalance.getAvailable())
                                .used(quotaBalance.getUsed())
                                .refunded(quotaBalance.getRefunded())
                                .issued(quotaBalance.getIssued())
                                .build()
                )
                .build();
    }
}
