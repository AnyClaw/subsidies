package ru.practice.subsidies.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practice.subsidies.dto.request.FlightInfoDto;
import ru.practice.subsidies.dto.response.FlightSearchResult;
import ru.practice.subsidies.repository.FlightRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubsidiesService {

    private final FlightRepository flightRepository;

    public List<FlightSearchResult> findFlights(FlightInfoDto flightInfo) {
        return flightRepository.findAvailableFlightsByRoute(flightInfo.from(), flightInfo.to());
    }
}
