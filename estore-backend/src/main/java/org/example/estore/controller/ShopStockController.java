package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.ShopStockDto;
import org.example.estore.service.ShopStockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stock")
@Tag(name = "Наличие товара в магазинах")
public class ShopStockController {

    private final ShopStockService service;

    public ShopStockController(ShopStockService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Постраничный список остатков")
    public Page<ShopStockDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Задать количество товара в магазине (создание/обновление)")
    public ShopStockDto save(@Valid @RequestBody ShopStockDto dto) {
        return service.save(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить запись о наличии")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
