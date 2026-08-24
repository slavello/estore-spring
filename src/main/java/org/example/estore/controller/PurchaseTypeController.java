package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.PurchaseTypeDto;
import org.example.estore.service.PurchaseTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/purchase-types")
@Tag(name = "Типы покупок", description = "Справочник \"Тип покупки\"")
public class PurchaseTypeController {

    private final PurchaseTypeService service;

    public PurchaseTypeController(PurchaseTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список типов покупок")
    public Page<PurchaseTypeDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Тип покупки по идентификатору")
    public PurchaseTypeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить тип покупки")
    public PurchaseTypeDto create(@Valid @RequestBody PurchaseTypeDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить тип покупки")
    public PurchaseTypeDto update(@PathVariable Long id, @Valid @RequestBody PurchaseTypeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип покупки")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
