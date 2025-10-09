package com.marko.anonymous_feedback_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.anonymous_feedback_bot.model.Role;
import com.marko.anonymous_feedback_bot.model.Sentiment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeminiService {

    @Value("${google.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // Тепер record має 3 поля: sentiment, severity, advice
    public record AnalysisResult(Sentiment sentiment, int severity, String advice) {}

    public AnalysisResult analyzeText(String text, Role role) {
        try {
            String prompt = """
                Проаналізуй цей текст відгуку від працівника або клієнта і поверни суворо JSON такого формату:

                {
                  "tone": "POSITIVE" або "NEUTRAL" або "NEGATIVE",
                  "criticalLevel": число від 1 до 5,
                  "advice": "коротка практична порада (до 12 слів, українською мовою)"
                }

                Інструкції:
                1. Тон ("tone") визначай за емоційністю та контекстом.
                2. CriticalLevel:
                   - 1–2: все добре або дрібні зауваження
                   - 3: нейтральна оцінка, є пропозиції
                   - 4–5: негатив, серйозна проблема чи невдоволення
                3. Поле "advice" — коротка порада у відповідь на відгук:
                   - максимум 12 слів
                   - не загальна, а конкретна до ситуації
                   - приклади: "Продовжуйте працювати в тому ж дусі", "Варто покращити комунікацію з клієнтами",
                   у випадку якщо порада не доречна то просто повинно бути "-", 
                   це якщо повідомлення наприклад комплімент чи просто те чого не можна покращити,
                   Це порада для начальства користувач цього бачити не буде, порада, щоб керівництво знало, що можна покращити
                4. Відповідь має бути **тільки JSON**, без пояснень і додаткового тексту.

                Роль користувача: "%s"
                Відгук: "%s"
                """.formatted(role, text);

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = GEMINI_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Очищаємо JSON від ```json форматування
            content = content.trim()
                    .replaceAll("^```(?:json)?", "")
                    .replaceAll("```$", "")
                    .trim();

            JsonNode node = mapper.readTree(content);
            String tone = node.get("tone").asText().toUpperCase();
            int critical = node.get("criticalLevel").asInt();
            String advice = node.has("advice") ? node.get("advice").asText() : "Без поради";

            Sentiment sentiment = switch (tone) {
                case "POSITIVE", "ПОЗИТИВНИЙ" -> Sentiment.POSITIVE;
                case "NEGATIVE", "НЕГАТИВНИЙ" -> Sentiment.NEGATIVE;
                default -> Sentiment.NEUTRAL;
            };

            return new AnalysisResult(sentiment, Math.max(1, Math.min(critical, 5)), advice);

        } catch (Exception e) {
            System.err.println("Помилка GeminiService: " + e.getMessage());
            return new AnalysisResult(Sentiment.NEUTRAL, 3, "Не вдалося отримати пораду");
        }
    }
}

// Очікувана відповідь

// Я дуже задоволений сервісом! Все працює швидко та зручно.
// Sentiment: POSITIVE // CriticalLevel: 1-2
// -

// Сервіс нормальний, але можна зробити інтерфейс більш зрозумілим.
// Sentiment: NEUTRAL // CriticalLevel: 2-3
// Спрощення інтерфейсу зробить користування ще зручнішим.


// Мене розчарувало обслуговування, довго чекали на відповідь і нічого не вирішили.
// Sentiment: NEGATIVE // CriticalLevel: 4-5
// Варто покращити швидкість і якість підтримки клієнтів.