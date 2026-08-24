package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.PositionDto;
import org.example.estore.service.PositionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/positions")
@Tag(name = "Должности", description = "Справочник \"Должности\"")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список должностей")
    public Page<PositionDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Должность по идентификатору")
    public PositionDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить должность")
    public PositionDto create(@Valid @RequestBody PositionDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить должность")
    public PositionDto update(@PathVariable Long id, @Valid @RequestBody PositionDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить должность")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
