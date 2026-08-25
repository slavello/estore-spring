package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.ImportResultDto;
import org.example.estore.service.CsvImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Импорт данных:
 * <ul>
 *     <li>POST /api/import — zip-архив с CSV-файлами (все таблицы разом);</li>
 *     <li>POST /api/import/{таблица} — одиночный CSV-файл для конкретной таблицы.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/import")
@Tag(name = "Импорт данных", description = "Загрузка данных из zip-архива с CSV-файлами и отдельных CSV-файлов")
public class ImportController {

    private final CsvImportService service;

    public ImportController(CsvImportService service) {
        this.service = service;
    }

    @PostMapping(value = "/positions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт справочника \"Должности\" из CSV-файла")
    public ImportResultDto importPositions(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "positions");
    }

    @PostMapping(value = "/shops", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт справочника \"Магазин\" из CSV-файла")
    public ImportResultDto importShops(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "shops");
    }

    @PostMapping(value = "/electronics-types", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт справочника \"Тип электроники\" из CSV-файла")
    public ImportResultDto importElectronicsTypes(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "electronics_types");
    }

    @PostMapping(value = "/purchase-types", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт справочника \"Тип покупки\" из CSV-файла")
    public ImportResultDto importPurchaseTypes(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "purchase_types");
    }

    @PostMapping(value = "/employees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт реестра \"Сотрудники\" из CSV-файла")
    public ImportResultDto importEmployees(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "employees");
    }

    @PostMapping(value = "/electronics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт реестра \"Электротовары\" из CSV-файла")
    public ImportResultDto importElectronics(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "electronics");
    }

    @PostMapping(value = "/stock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт остатков товаров в магазинах из CSV-файла")
    public ImportResultDto importStocks(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "stocks");
    }

    @PostMapping(value = "/purchases", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импорт реестра \"Покупки\" из CSV-файла")
    public ImportResultDto importPurchases(@RequestParam("file") MultipartFile file) {
        return service.importCsv(file, "purchases");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импортировать данные из zip-архива с CSV-файлами")
    public ImportResultDto importZip(@RequestParam("file") MultipartFile file) {
        return service.importZip(file);
    }

}
