package ru.practice.subsidies.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practice.subsidies.dto.request.InsertRequest;
import ru.practice.subsidies.dto.response.InsertResponse;
import ru.practice.subsidies.dto.response.QuotaBalanceDto;
import ru.practice.subsidies.entity.*;
import ru.practice.subsidies.enums.PassengerCategory;
import ru.practice.subsidies.enums.TicketStatus;
import ru.practice.subsidies.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketUtils {

    private final QuotaBalanceRepository quotaBalanceRepository;
    private final FlightRepository flightRepository;
    private final TicketRepository ticketRepository;
    private final PassengerRepository passengerRepository;
    private final TicketOperationRepository ticketOperationRepository;
    private final ResidentRepository residentRepository;

    public InsertResponse makeOperationWithMoroshkaTicket(InsertRequest insertRequest) {
        Optional<Flight> flightOp = flightRepository.findById(insertRequest.ticket().flightId());

        if (flightOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Рейс с id = " + insertRequest.ticket().flightId() + " не субсидируется!")
                    .build();
        }

        Flight flight = flightOp.get();

        Optional<QuotaBalance> quotaBalanceOp = quotaBalanceRepository
                .findByResidentDocumentAndYear(
                        insertRequest.passenger().documentType(),
                        insertRequest.passenger().documentNumber(),
                        flight.getFlightDate().getYear()
                );

        if (quotaBalanceOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Пользователь не является обладателем карты МОРОШКА!")
                    .build();
        }

        QuotaBalance quotaBalance = quotaBalanceOp.get();

        return switch (insertRequest.operationType()) {
            case ISSUED -> issueMoroshkaTicket(insertRequest, quotaBalance, flight);
            case REFUNDED -> refundMoroshkaTicket(insertRequest, quotaBalance);
            case USED -> useMoroshkaTicket(insertRequest, quotaBalance);
        };
    }

    public InsertResponse makeOperationWithFederalTicket(InsertRequest insertRequest) {
        return switch (insertRequest.operationType()) {
            case ISSUED -> issueFederalTicket(insertRequest);
            case REFUNDED -> refundFederalTicket(insertRequest);
            case USED -> useFederalTicket(insertRequest);
        };
    }

    private InsertResponse issueMoroshkaTicket(InsertRequest insertRequest, QuotaBalance quotaBalance, Flight flight) {
        if (quotaBalance.getRemaining() <= 0) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("У пользователя закончился баланс на карте МОРОШКА!")
                    .build();
        }

        quotaBalance.plusIssued();
        quotaBalanceRepository.save(quotaBalance);

        if (ticketRepository.existsByTicketNumber(insertRequest.ticket().number())) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже продан!")
                    .build();
        }

        Ticket ticket = Ticket.builder()
                .ticketNumber(insertRequest.ticket().number())
                .flight(flight)
                .fareType(insertRequest.ticket().fareType())
                .totalPriceKopecks(insertRequest.ticket().totalPriceKopecks())
                .status(insertRequest.operationType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticketRepository.save(ticket);

        int age = Period.between(insertRequest.passenger().birthday(), LocalDate.now()).getYears();
        PassengerCategory category = age <= 12 ? PassengerCategory.CHILD_2_12 : PassengerCategory.ADULT;

        Passenger passenger = Passenger.builder()
                .ticket(ticket)
                .resident(quotaBalance.getResident())
                .surname(quotaBalance.getResident().getSurname())
                .name(quotaBalance.getResident().getName())
                .patronymic(quotaBalance.getResident().getPatronymic())
                .birthday(quotaBalance.getResident().getBirthday())
                .category(category)
                .documentType(quotaBalance.getResident().getDocumentType())
                .documentNumber(quotaBalance.getResident().getDocumentNumber())
                .build();

        passengerRepository.save(passenger);
        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
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

    private InsertResponse refundMoroshkaTicket(InsertRequest insertRequest, QuotaBalance quotaBalance) {
        Optional<Ticket> ticketOp = ticketRepository.findByTicketNumber(insertRequest.ticket().number());

        if (ticketOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билета с номером " + insertRequest.ticket().number() + " не найден!")
                    .build();
        }

        Ticket ticket = ticketOp.get();

        if (ticket.getStatus() == TicketStatus.USED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже использован!")
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже возвращен!")
                    .build();
        }

        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        quotaBalance.plusRefunded();
        quotaBalanceRepository.save(quotaBalance);

        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
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

    private InsertResponse useMoroshkaTicket(InsertRequest insertRequest, QuotaBalance quotaBalance) {
        Optional<Ticket> ticketOp = ticketRepository.findByTicketNumber(insertRequest.ticket().number());

        if (ticketOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билета с номером " + insertRequest.ticket().number() + " не найден!")
                    .build();
        }

        Ticket ticket = ticketOp.get();

        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже возвращен!")
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже использован!")
                    .build();
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        quotaBalance.plusUsed();
        quotaBalanceRepository.save(quotaBalance);

        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
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

    private InsertResponse issueFederalTicket(InsertRequest insertRequest) {
        Optional<Flight> flightOp = flightRepository.findById(insertRequest.ticket().flightId());

        if (flightOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Рейс с id = " + insertRequest.ticket().flightId() + " не субсидируется!")
                    .build();
        }

        Flight flight = flightOp.get();

        if (ticketRepository.existsByTicketNumber(insertRequest.ticket().number())) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже продан!")
                    .build();
        }

        Ticket ticket = Ticket.builder()
                .ticketNumber(insertRequest.ticket().number())
                .flight(flight)
                .fareType(insertRequest.ticket().fareType())
                .totalPriceKopecks(insertRequest.ticket().totalPriceKopecks())
                .status(insertRequest.operationType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticketRepository.save(ticket);

        int age = Period.between(insertRequest.passenger().birthday(), LocalDate.now()).getYears();
        PassengerCategory category = age <= 12 ? PassengerCategory.CHILD_2_12 : PassengerCategory.ADULT;

        Resident resident = residentRepository.findByResidentDto(insertRequest.passenger()).orElse(null);

        Passenger passenger = Passenger.builder()
                .ticket(ticket)
                .resident(resident)
                .surname(insertRequest.passenger().surname())
                .name(insertRequest.passenger().name())
                .patronymic(insertRequest.passenger().patronymic())
                .birthday(insertRequest.passenger().birthday())
                .category(category)
                .documentType(insertRequest.passenger().documentType())
                .documentNumber(insertRequest.passenger().documentNumber())
                .build();

        passengerRepository.save(passenger);
        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
                .build();
    }

    private InsertResponse refundFederalTicket(InsertRequest insertRequest) {
        Optional<Ticket> ticketOp = ticketRepository.findByTicketNumber(insertRequest.ticket().number());

        if (ticketOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билета с номером " + insertRequest.ticket().number() + " не найден!")
                    .build();
        }

        Ticket ticket = ticketOp.get();

        if (ticket.getStatus() == TicketStatus.USED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже использован!")
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже возвращен!")
                    .build();
        }

        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
                .build();
    }

    private InsertResponse useFederalTicket(InsertRequest insertRequest) {
        Optional<Ticket> ticketOp = ticketRepository.findByTicketNumber(insertRequest.ticket().number());

        if (ticketOp.isEmpty()) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билета с номером " + insertRequest.ticket().number() + " не найден!")
                    .build();
        }

        Ticket ticket = ticketOp.get();

        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже возвращен!")
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            return InsertResponse.builder()
                    .status("FAILED")
                    .message("Билет с номером " + insertRequest.ticket().number() + " уже использован!")
                    .build();
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketOperation ticketOperation = makeTicketOperation(ticket);

        return InsertResponse.builder()
                .operationId(ticketOperation.getId())
                .status("SUCCESS")
                .build();
    }

    private TicketOperation makeTicketOperation(Ticket ticket) {
        return ticketOperationRepository.save(
                TicketOperation.builder()
                        .ticket(ticket)
                        .operationType(ticket.getStatus())
                        .operationDateTime(LocalDateTime.now())
                        .build()
        );
    }
}
