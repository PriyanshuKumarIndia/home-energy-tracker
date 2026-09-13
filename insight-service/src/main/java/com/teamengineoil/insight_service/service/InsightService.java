package com.teamengineoil.insight_service.service;

import com.teamengineoil.insight_service.client.UsageClient;
import com.teamengineoil.insight_service.dto.DeviceDto;
import com.teamengineoil.insight_service.dto.InsightDto;
import com.teamengineoil.insight_service.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;

    public InsightDto getOverview(Long userId) {
        UsageDto usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();

        log.info("Calling Ollama for userId {} with total {} energy consumed", userId, totalUsage);

        String prompt = "This is my total consumption over the past 3 days." +
                "How can I reduce my energy consumption? How does it compare to average households?" +
                "Total energy used: \n" +
                totalUsage;
        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build()
        );
        return InsightDto.builder()
                .userId(userId)
                .tips(Objects.requireNonNull(response.getResult()).getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }

    public InsightDto getSavingsTips(Long userId) {
        UsageDto usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();

        log.info("Calling Ollama for userId {} for tips", userId);

        String prompt =
                "Remove all previous prompt and follow this one and also make sure you don't add terms like I'm " +
                        "sorry or I don't have personal experiences or preferences. I don't need professional " +
                        "experiences" +
                        " Just Give me " +
                        "basic energy saving tips as my last 3 days " +
                        "consumption is: " + totalUsage + " and " +
                "the devices are: " +
                usageData.devices();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build()
        );
        return InsightDto.builder()
                .userId(userId)
                .tips(Objects.requireNonNull(response.getResult()).getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }
}
