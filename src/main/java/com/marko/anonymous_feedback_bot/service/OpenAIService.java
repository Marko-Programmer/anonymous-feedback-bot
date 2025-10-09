//package com.marko.anonymous_feedback_bot.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.marko.anonymous_feedback_bot.model.Sentiment;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class OpenAIService {
//
//    @Value("${openai.api.key}")
//    private String apiKey;
//
//    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//
//    private final Semaphore semaphore = new Semaphore(1);
//
//    private long lastRequestTime = 0;
//
//    public record AnalysisResult(Sentiment sentiment, int severity) {}
//
//    public AnalysisResult analyzeText(String text) {
//        try {
//            long now = System.currentTimeMillis();
//            long waitTime = 20000 - (now - lastRequestTime);
//            if (waitTime > 0) Thread.sleep(waitTime);
//
//            semaphore.acquire();
//
//            String prompt = """
//                    Проаналізуй цей текст і поверни строго JSON у форматі:
//                    {
//                      "tone": "POSITIVE" або "NEUTRAL" або "NEGATIVE",
//                      "criticalLevel": число від 1 до 5
//                    }
//
//                    Текст: "%s"
//                    """.formatted(text);
//
//            Map<String, Object> requestBody = Map.of(
//                    "model", "gpt-3.5-turbo",
//                    "messages", List.of(
//                            Map.of("role", "user", "content", prompt)
//                    ),
//                    "max_tokens", 100
//            );
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(apiKey);
//
//            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<Map> response;
//            try {
//                response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
//            } catch (HttpClientErrorException.TooManyRequests e) {
//                System.err.println("⚠️ Too Many Requests, повторимо через 5 секунд...");
//                Thread.sleep(5000);
//                return analyzeText(text);
//            }
//
//            lastRequestTime = System.currentTimeMillis();
//            semaphore.release();
//
//            String content = (String) ((Map<String, Object>)
//                    ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0))
//                            .get("message"))
//                    .get("content");
//
//
//            JsonNode node = mapper.readTree(content);
//            String tone = node.get("tone").asText().toUpperCase();
//            int critical = node.get("criticalLevel").asInt();
//
//            Sentiment sentiment = switch (tone) {
//                case "POSITIVE", "ПОЗИТИВНИЙ" -> Sentiment.POSITIVE;
//                case "NEGATIVE", "НЕГАТИВНИЙ" -> Sentiment.NEGATIVE;
//                default -> Sentiment.NEUTRAL;
//            };
//
//            return new AnalysisResult(sentiment, Math.max(1, Math.min(critical, 5)));
//
//        } catch (Exception e) {
//            System.err.println("⚠️ Помилка OpenAIService: " + e.getMessage());
//            return new AnalysisResult(Sentiment.NEUTRAL, 3); // fallback
//        }
//    }
//}
