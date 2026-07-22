package ru.practice.subsidies.controller.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.subsidies.controller.SubsidiesController;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.SelectResponse;
import ru.practice.subsidies.service.SubsidiesService;

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
