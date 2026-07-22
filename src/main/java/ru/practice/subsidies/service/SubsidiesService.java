package ru.practice.subsidies.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practice.subsidies.dto.request.InsertRequest;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.*;
import ru.practice.subsidies.entity.*;
import ru.practice.subsidies.enums.PassengerCategory;
import ru.practice.subsidies.enums.SubsidyProgram;
import ru.practice.subsidies.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final MoroshkaTariffRepository moroshkaTariffRepository;
    private final ResidentRepository residentRepository;
    private final TicketRepository ticketRepository;
    private final PassengerRepository passengerRepository;
    private final TicketOperationRepository ticketOperationRepository;

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

        int age = Period.between(selectRequest.residentInfo().birthday(), currentDate).getYears();
        Float isChild = age >= 2 && age <= 12 ? 0.5f : 1;
        isChild = age < 2 ? 0 : isChild;

        if (quotaBalanceOp.isEmpty() || !quotaBalanceOp.get().getResident().getHasCard()) {
            return SelectResponse.builder()
                    .confirmationInfo(
                            ConfirmationInfoDto.builder()
                                    .type(SubsidyProgram.FEDERAL)
                                    .status("CONFIRMED")
                                    .priceKopecks((int)(moroshkaTariff.getAdultBasePriceKopecks() * isChild))
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
                                .priceKopecks((int)(moroshkaTariff.getAdultCardPriceKopecks() * isChild))
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
        Optional<Resident> residentOp = residentRepository.findByResidentDto(insertRequest.passenger());

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

    public List<FlightDto> getFlights() {
        return flightRepository.findAll().stream()
                .map(
                        flight -> FlightDto.builder()
                                .id(flight.getId())
                                .flightNumber(flight.getFlightNumber())
                                .departureLocation(flight.getRoute().getDepartureLocation().getCode())
                                .arrivalLocation(flight.getRoute().getDepartureLocation().getCode())
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
