package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.service.DataImportService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller för att trigga import av testdata.
 * Används bara under utveckling.
 */
@RestController
@RequestMapping("/api/import")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping("/example")
    public String importExampleData() {
        dataImportService.importExampleData();
        return "Example data imported successfully!";
    }
}
