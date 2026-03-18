package com.example.recheck.controller;

import com.example.recheck.dto.AiRequest;
import com.example.recheck.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class DeepSeekController {

    private final OpenAiChatModel chatModel;
    @PostMapping("/ai/generate")
    public AiResponse generate(@RequestBody AiRequest request) { // Renamed parameter to 'request' for clarity

        // 1. Create a converter for our record
        var converter = new BeanOutputConverter<>(AiResponse.class);

        // 2. Updated Prompt Template: Explicitly separate History and New Question
        String userPrompt = """
                You are a helpful AI Assistant. Use the provided history to give better, context-aware responses.
                
                CONVERSATION HISTORY:
                {context}
                
                USER'S LATEST MESSAGE:
                {message}
                
                FORMAT INSTRUCTIONS:
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userPrompt);

        // 3. Inject BOTH the context and the message into the prompt
        Prompt prompt = promptTemplate.create(Map.of(
                "context", request.getContext() == null || request.getContext().isEmpty() ? "No previous history." : request.getContext(),
                "message", request.getMessage(),
                "format", converter.getFormat()
        ));

        // 4. Call the model and convert the result
        ChatResponse response = chatModel.call(prompt);
        return converter.convert(Objects.requireNonNull(response.getResult().getOutput().getText()));
    }


    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(
            @RequestParam(defaultValue = "Tell me a joke") String message) {
        var prompt = new Prompt(message);
        return chatModel.stream(prompt);
    }
}