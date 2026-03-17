package com.example.recheck.dto;
import java.util.List;

/**
 * Modern, flexible record for AI responses.
 * Using generic names helps the AI avoid forcing markdown formatting.
 */
public record AiResponse(
        String title,
        String primaryContent, // Can be code, a summary, or a description
        List<String> insights,
        List<String> actionItems
) {}
