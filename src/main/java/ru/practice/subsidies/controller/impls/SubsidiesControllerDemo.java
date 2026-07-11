package ru.practice.subsidies.controller.impls;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.subsidies.controller.SubsidiesController;

@RestController
@Slf4j
public class SubsidiesControllerDemo implements SubsidiesController {

    @Override
    public String select() {
        return "select";
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
