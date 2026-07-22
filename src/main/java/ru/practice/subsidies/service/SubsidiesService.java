package ru.practice.subsidies.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practice.subsidies.dto.request.InsertRequest;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.*;
import ru.practice.subsidies.entity.Flight;
import ru.practice.subsidies.entity.MoroshkaTariff;
import ru.practice.subsidies.entity.QuotaBalance;
import ru.practice.subsidies.entity.Resident;
import ru.practice.subsidies.enums.SubsidyProgram;
import ru.practice.subsidies.repository.FlightRepository;
import ru.practice.subsidies.repository.MoroshkaTariffRepository;
import ru.practice.subsidies.repository.QuotaBalanceRepository;
import ru.practice.subsidies.repository.ResidentRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubsidiesService {

    private final TicketUtils ticketUtils;

    private final FlightRepository flightRepository;
    private final QuotaBalanceRepository quotaBalanceRepository;
    private final ResidentRepository residentRepository;
    private final MoroshkaTariffRepository moroshkaTariffRepository;

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

        if (!residentRepository.existsByDocumentTypeAndDocumentNumber(
                selectRequest.residentInfo().documentType(),
                selectRequest.residentInfo().documentNumber()
        )) {
            return SelectResponse.generateRefutedResponse("Пользователь не найден");
        }

        MoroshkaTariff moroshkaTariff = moroshkaTariffRepository
                .findByRoute_Id(flight.getRoute().getId())
                .orElseThrow();

        Optional<QuotaBalance> quotaBalanceOp = quotaBalanceRepository.findByResidentDocumentAndYear(
                selectRequest.residentInfo().documentType(),
                selectRequest.residentInfo().documentNumber(),
                currentDate.getYear()
        );

        int age = Period.between(selectRequest.residentInfo().birthday(), LocalDate.now()).getYears();
        float ageDiscount = age > 12 ? 1 : 0.5f;
        ageDiscount = age < 2 ? 0 : ageDiscount;

        if (quotaBalanceOp.isEmpty() || !quotaBalanceOp.get().getResident().getHasCard()) {
            return SelectResponse.builder()
                    .confirmationInfo(
                            ConfirmationInfoDto.builder()
                                    .type(SubsidyProgram.FEDERAL)
                                    .status("CONFIRMED")
                                    .priceKopecks((int)(moroshkaTariff.getAdultBasePriceKopecks() * ageDiscount))
                                    .build()
                    )
                    .build();
        }

        QuotaBalance quotaBalance = quotaBalanceOp.get();

        if (quotaBalance.getRemaining() <= 0) {
            return SelectResponse.generateRefutedResponse("Лимит исчерпан");
        }

        return SelectResponse.builder()
                .confirmationInfo(
                        ConfirmationInfoDto.builder()
                                .type(SubsidyProgram.MOROSHKA)
                                .status("CONFIRMED")
                                .priceKopecks((int)(moroshkaTariff.getAdultCardPriceKopecks() * ageDiscount))
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

    @Transactional
    public InsertResponse insert(InsertRequest insertRequest) {
        Optional<Resident> residentOp = residentRepository
                .findByResidentDto(insertRequest.passenger());

        if (insertRequest.ticket().fareType() == SubsidyProgram.MOROSHKA) {
            if (residentOp.isEmpty() || !residentOp.get().getHasCard()) {
                return InsertResponse.builder()
                        .status("FAILED")
                        .message("Пользователь не является обладателем карты МОРОШКА!")
                        .build();
            }
        }

        return switch (insertRequest.ticket().fareType()) {
            case MOROSHKA -> ticketUtils.makeOperationWithMoroshkaTicket(insertRequest);
            case FEDERAL -> ticketUtils.makeOperationWithFederalTicket(insertRequest);
        };
    }

    public List<FlightDto> findFlights() {
        return flightRepository.findAll().stream()
                .map(
                        flight -> FlightDto.builder()
                                .id(flight.getId())
                                .flightNumber(flight.getFlightNumber())
                                .departureLocation(flight.getRoute().getDepartureLocation().getCode())
                                .arrivalLocation(flight.getRoute().getArrivalLocation().getCode())
                                .flightDate(flight.getFlightDate())
                                .departureTime(flight.getDepartureTime())
                                .arrivalTime(flight.getArrivalTime())
                                .totalSeats(flight.getTotalSeats())
                                .availableSeats(flight.getAvailableSeats())
                                .distanceKm(flight.getRoute().getDistanceKm())
                                .maxPriceFedKopecks(flight.getRoute().getMaxPriceFedKopecks())
                                .build()
                )
                .toList();
    }
}
