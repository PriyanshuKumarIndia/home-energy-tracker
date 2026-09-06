package com.teamengineoil.ingestion_service.controller;

import com.teamengineoil.ingestion_service.dto.EnergyUsageDto;
import com.teamengineoil.ingestion_service.service.IngestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingestion")
public class IngestionController {
    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyUsageDto usageDto) {
        ingestionService.ingestEnergyUsage(usageDto);
    }
}
