package ru.practice.subsidies.controller.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.subsidies.controller.SubsidiesController;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.FlightSearchResult;
import ru.practice.subsidies.service.SubsidiesService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SubsidiesControllerDemo implements SubsidiesController {

    private final SubsidiesService subsidiesService;

    @Override
    public String select(SelectRequest selectRequest) {
        List<FlightSearchResult> results = subsidiesService.findFlights(
                selectRequest.flightInfo()
        );
        log.info("Total found: {}", results.size());
        return results.toString();
    }

    @Override
    public String insert() {
        return "insert";
    }

    @Override
    public String batch() {
        return "batch";
    }

    @Override
    public String search() {
        return "search";
    }

    @Override
    public String delete() {
        return "delete";
    }
}
