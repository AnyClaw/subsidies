package ru.practice.subsidies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.subsidies.entity.TicketOperation;

public interface TicketOperationRepository extends JpaRepository<TicketOperation, Integer> {
}
