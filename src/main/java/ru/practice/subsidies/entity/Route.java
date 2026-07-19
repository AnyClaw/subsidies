package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "routes")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer id;

    private String program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depart_loc_id")
    private Location departureLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrive_loc_id")
    private Location arrivalLocation;

    private Integer distanceKm;
    private Integer maxPriceFedKopecks;
    private Boolean isBidirectional;
}
