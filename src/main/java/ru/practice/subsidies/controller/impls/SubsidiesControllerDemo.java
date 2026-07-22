package ru.practice.subsidies.controller.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.subsidies.controller.SubsidiesController;
import ru.practice.subsidies.dto.request.InsertRequest;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.FlightDto;
import ru.practice.subsidies.dto.response.InsertResponse;
import ru.practice.subsidies.dto.response.SelectResponse;
import ru.practice.subsidies.service.SubsidiesService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SubsidiesControllerDemo implements SubsidiesController {

    private final SubsidiesService subsidiesService;

    @Override
    public SelectResponse select(SelectRequest selectRequest) {
        log.info("GET: {}", selectRequest.toString());
        return subsidiesService.select(selectRequest);
    }

    @Override
    public InsertResponse insert(InsertRequest insertRequest) {
        return subsidiesService.insert(insertRequest);
    }

    @Override
    public List<FlightDto> getFlights() {
        return subsidiesService.getFlights();
    }
}
