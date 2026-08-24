package org.example.estore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.estore.dto.ImportResultDto;
import org.example.estore.service.CsvImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Импорт данных", description = "Загрузка данных из zip-архива с CSV-файлами")
public class ImportController {

    private final CsvImportService service;

    public ImportController(CsvImportService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Импортировать данные из zip-архива с CSV-файлами")
    public ImportResultDto importZip(@RequestParam("file") MultipartFile file) {
        return service.importZip(file);
    }
}
