package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practice.subsidies.enums.TicketStatus;

import java.time.LocalDateTime;

@Table(name = "ticket_operations")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TicketOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_operation_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private TicketStatus operationType;

    @Column(name = "operation_dt")
    private LocalDateTime operationDateTime;
}
