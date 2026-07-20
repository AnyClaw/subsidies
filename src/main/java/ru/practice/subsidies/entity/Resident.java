package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "residents")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resident_id")
    private Integer id;

    @Column(name = "last_name")
    private String surname;

    @Column(name = "first_name")
    private String name;

    @Column(name = "middle_name")
    private String patronymic;

    @Column(name = "birthdate")
    private LocalDate birthday;

    private String documentType;
    private String documentNumber;
    private Boolean hasCard;
}
