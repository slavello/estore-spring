package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.ElectronicsDto;
import org.example.estore.service.ElectronicsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/electronics")
@Tag(name = "Электротовары", description = "Реестр \"Электротовары\"")
public class ElectronicsController {

    private final ElectronicsService service;

    public ElectronicsController(ElectronicsService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список электротоваров")
    public Page<ElectronicsDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Электротовар по идентификатору")
    public ElectronicsDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить электротовар")
    public ElectronicsDto create(@Valid @RequestBody ElectronicsDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить электротовар")
    public ElectronicsDto update(@PathVariable Long id, @Valid @RequestBody ElectronicsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить электротовар")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
