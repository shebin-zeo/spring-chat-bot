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
//    public AiResponse generate(@RequestParam(defaultValue = "Explain Hello World in Java") String message) {
    public AiResponse generate(@RequestBody AiRequest message){

        // 1. Create a converter for our record
        var converter = new BeanOutputConverter<>(AiResponse.class);

        // 2. Build a prompt template that tells the AI exactly what format to use
        String userPrompt = """
                {message}
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "message", message,
                "format", converter.getFormat() // This injects the JSON schema instructions
        ));

        // 3. Call the model and convert the result
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