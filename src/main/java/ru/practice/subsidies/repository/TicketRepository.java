package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.subsidies.entity.Ticket;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Boolean existsByTicketNumber(String ticketNumber);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
