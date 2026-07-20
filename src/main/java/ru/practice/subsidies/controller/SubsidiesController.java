package ru.practice.subsidies.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import ru.practice.subsidies.dto.request.SelectRequest;
import ru.practice.subsidies.dto.response.FlightDto;
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
    String insert();

    @PostMapping("/batch")
    @Operation(
            summary = "Массовая загрузка операций",
            description = """
                    Метод предназначен для массовой загрузки списка операций в отложенном режиме
                    (например, за прошедший период). Аналогичен методу /insert, но принимает массив операций.
                    """
    )
    String batch();

    @GetMapping("/search")
    @Operation(
            summary = "Поиск операций",
            description = """
                    Метод предназначен для поиска операций по билету или пассажиру.
                    """
    )
    String search();

    @DeleteMapping("/delete")
    @Operation(
            summary = "Удаление ошибочных записей",
            description = """
                    Метод предназначен для удаления ошибочных записей перед последующей перевыгрузкой
                    корректных данных. Удаление происходит по совокупности полей.
                    """
    )
    String delete();

    @GetMapping("/flights")
    @Operation(
            summary = "Получение рейсов",
            description = "Метод для получения всех субсидируемых рейсов"
    )
    List<FlightDto> getFlights();
}
