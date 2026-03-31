package com.example.demo.remastering.service;

import com.example.demo.remastering.dto.RemasteringLogResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RemasteringService {

    private final AtomicLong sequence = new AtomicLong(1L);

    public RemasteringLogResponse remaster(
            Long sessionId,
            MultipartFile audioFile,
            String preferredTone
    ) {
        long logId = sequence.getAndIncrement();

        String originalSpeech = "결제... 해줄... 거야?";
        String refinedText = buildMockRefinedText(preferredTone);
        int latencyMs = ThreadLocalRandom.current().nextInt(700, 1201);

        return new RemasteringLogResponse(
                logId,
                originalSpeech,
                refinedText,
                latencyMs
        );
    }

    private String buildMockRefinedText(String preferredTone) {
        if (preferredTone == null || preferredTone.isBlank()) {
            return "결제 도와드릴까요?";
        }

        String tone = preferredTone.trim().toLowerCase();

        return switch (tone) {
            case "gentle", "soft", "부드럽게" -> "결제 도와드릴까요?";
            case "formal", "정중하게" -> "결제를 도와드릴까요?";
            case "friendly", "친근하게" -> "결제 도와드릴까요?";
            default -> "결제 도와드릴까요?";
        };
    }
}