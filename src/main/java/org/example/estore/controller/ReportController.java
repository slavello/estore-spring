package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Лучшие сотрудники, продажи умных часов, выручка наличными")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    /**
     * Лучший сотрудник в каждой должности за последний год.
     *
     * @param criterion
     *     "count" (по умолчанию) — количество проданных товаров;
     *     "sum" — сумма проданных товаров за последний год
     */
    @GetMapping("/best-employees")
    @Operation(summary = "Лучшие сотрудники по должности за последний год "
            + "(criterion=count — по количеству проданных товаров; criterion=sum — по сумме)")
    public List<BestEmployeeDto> bestEmployees(
            @Parameter(description = "Критерий: count | sum")
            @RequestParam(defaultValue = "count") String criterion) {
        return service.bestEmployeesByPosition(criterion);
    }

    /** Лучший младший продавец-консультант, продавший больше всех умных часов */
    @GetMapping("/best-smartwatch-seller")
    @Operation(summary = "Лучший младший продавец-консультант по продажам умных часов")
    public List<BestEmployeeDto> bestSmartWatchSeller() {
        return service.bestSmartWatchSellers();
    }

    /** Сумма денежных средств, полученная через оплату наличными (по сети или магазину) */
    @GetMapping("/cash-total")
    @Operation(summary = "Сумма денежных средств, полученная магазином через оплату наличными "
            + "(без shopId — по всей сети)")
    public CashTotalDto cashTotal(@RequestParam(required = false) Long shopId) {
        return service.cashTotal(shopId);
    }
}
