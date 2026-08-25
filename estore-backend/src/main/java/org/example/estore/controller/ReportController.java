package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Лучшие сотрудники, продажи выбранного товара, выручка по типам оплаты")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    /**
     * Лучший сотрудник в каждой должности за период с начальной даты.
     *
     * @param criterion
     *     "count" (по умолчанию) — количество проданных товаров;
     *     "sum" — сумма проданных товаров
     */
    @GetMapping("/best-employees")
    @Operation(summary = "Лучшие сотрудники по должности за период "
            + "(criterion=count — по количеству проданных товаров; criterion=sum — по сумме; "
            + "без startDate — последний год)")
    public List<BestEmployeeDto> bestEmployees(
            @Parameter(description = "Критерий: count | sum")
            @RequestParam(defaultValue = "count") String criterion,
            @Parameter(description = "Начальная дата периода, ГГГГ-ММ-ДД (не указана — последний год)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return service.bestEmployeesByPosition(criterion, startDate);
    }

    /** Лучший сотрудник выбранной должности, продавший больше всех выбранного товара за период */
    @GetMapping("/best-product-seller")
    @Operation(summary = "Лучший сотрудник выбранной должности по продажам выбранного товара за период "
            + "(electronicsId — электротовар; positionId — должность; без startDate — всё время)")
    public List<BestEmployeeDto> bestProductSeller(
            @Parameter(description = "Идентификатор электротовара")
            @RequestParam Long electronicsId,
            @Parameter(description = "Идентификатор должности")
            @RequestParam Long positionId,
            @Parameter(description = "Начальная дата периода, ГГГГ-ММ-ДД (не указана — всё время)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return service.bestProductSellers(electronicsId, positionId, startDate);
    }

    /** Сумма денежных средств, полученная через оплату выбранным типом за период (по сети или магазину) */
    @GetMapping("/cash-total")
    @Operation(summary = "Сумма денежных средств, полученная через оплату выбранным типом оплаты "
            + "(purchaseTypeId — тип покупки; без shopId — по всей сети; без startDate — всё время)")
    public CashTotalDto cashTotal(
            @Parameter(description = "Тип покупки (способ оплаты)")
            @RequestParam Long purchaseTypeId,
            @Parameter(description = "Магазин (не указан — вся сеть)")
            @RequestParam(required = false) Long shopId,
            @Parameter(description = "Начальная дата периода, ГГГГ-ММ-ДД (не указана — всё время)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return service.cashTotal(shopId, purchaseTypeId, startDate);
    }
}
