package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practice.subsidies.enums.PassengerCategory;

import java.time.LocalDate;

@Table(name = "passengers")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passenger_id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @Column(name = "last_name")
    private String surname;
    
    @Column(name = "first_name")
    private String name;
    
    @Column(name = "middle_name")
    private String patronymic;

    @Column(name = "birthdate")
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private PassengerCategory category;

    private String documentType;
    private String documentNumber;
}
