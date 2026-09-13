package com.teamengineoil.insight_service.controller;

import com.teamengineoil.insight_service.dto.InsightDto;
import com.teamengineoil.insight_service.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insight")
@RequiredArgsConstructor
public class InsightController {
    private final InsightService insightService;

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightDto> getSavingTips(@PathVariable Long userId) {
        final InsightDto insight = insightService.getSavingsTips(userId);
        return ResponseEntity.ok(insight);
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightDto> getOverview(@PathVariable Long userId) {
        final InsightDto insight = insightService.getOverview(userId);
        return ResponseEntity.ok(insight);
    }
}
