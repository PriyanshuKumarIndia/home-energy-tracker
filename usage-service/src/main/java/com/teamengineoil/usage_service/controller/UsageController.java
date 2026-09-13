package com.teamengineoil.usage_service.controller;

import com.teamengineoil.usage_service.dto.UsageDto;
import com.teamengineoil.usage_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDto> getUserDeviceUsage(@PathVariable("userId") Long userId,
                                                       @RequestParam(defaultValue = "3") int days) {
        UsageDto usage = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usage);
    }
}
