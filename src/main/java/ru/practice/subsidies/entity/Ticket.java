package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practice.subsidies.enums.SubsidyProgram;
import ru.practice.subsidies.enums.TicketStatus;

import java.time.LocalDateTime;

@Table(name = "tickets")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer id;

    private String ticketNumber;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Enumerated(EnumType.STRING)
    private SubsidyProgram fareType;

    private Integer totalPriceKopecks;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
