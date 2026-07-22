package ru.practice.subsidies.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import ru.practice.subsidies.dto.request.InsertRequest;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.FlightDto;
import ru.practice.subsidies.dto.response.InsertResponse;
import ru.practice.subsidies.dto.response.SelectResponse;

import java.util.List;

// TODO: переписать методы в соответствии со спецификацией

@RestController
@RequestMapping("/api/v1")
public interface SubsidiesController {

    @PostMapping("/select")
    @Operation(
            summary = "Проверка права пассажира на субсидию",
            description = """
                    Метод проверяет, имеет ли пассажир право на покупку билета по региональному
                    субсидированному тарифу, и возвращает актуальные балансы квот.
                    """
    )
    SelectResponse select(
            @RequestBody SelectRequest selectRequest
    );

    @PostMapping("/insert")
    @Operation(
            summary = "Фиксация операции с билетом",
            description = """
                    Метод регистрирует операцию с субсидированным билетом
                    (продажа, возврат, использование, обмен, редактирование) и обновляет балансы квот.
                    """
    )
    InsertResponse insert(
            @RequestBody InsertRequest insertRequest
    );

    @GetMapping("/flights")
    List<FlightDto> findFlights();
}
