package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.PurchaseDto;
import org.example.estore.service.PurchaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Покупки", description = "Реестр \"Покупки\"")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список покупок с сортировкой по дате")
    public Page<PurchaseDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Покупка по идентификатору")
    public PurchaseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оформить покупку (с проверкой наличия товара в магазине)")
    public PurchaseDto create(@Valid @RequestBody PurchaseDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить покупку")
    public PurchaseDto update(@PathVariable Long id, @Valid @RequestBody PurchaseDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить покупку (товар возвращается на склад)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
