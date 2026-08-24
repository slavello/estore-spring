package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.ElectronicsTypeDto;
import org.example.estore.service.ElectronicsTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/electronics-types")
@Tag(name = "Типы электроники", description = "Справочник \"Тип электроники\"")
public class ElectronicsTypeController {

    private final ElectronicsTypeService service;

    public ElectronicsTypeController(ElectronicsTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список типов электроники")
    public Page<ElectronicsTypeDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Тип электроники по идентификатору")
    public ElectronicsTypeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить тип электроники")
    public ElectronicsTypeDto create(@Valid @RequestBody ElectronicsTypeDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить тип электроники")
    public ElectronicsTypeDto update(@PathVariable Long id, @Valid @RequestBody ElectronicsTypeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип электроники")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
