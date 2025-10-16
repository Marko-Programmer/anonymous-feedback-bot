package com.marko.anonymous_feedback_bot.service;

import com.marko.anonymous_feedback_bot.model.Feedback;
import com.marko.anonymous_feedback_bot.model.Role;
import com.marko.anonymous_feedback_bot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final GeminiService geminiService;
    private final GoogleSheetsService googleSheetsService;
    private final TrelloService trelloService;
    // private final GoogleDocsService googleDocsService;

    public Feedback processAndSaveFeedback(String message, Role role, String branch) {
        var analysis = geminiService.analyzeText(message, role);


        Feedback feedback = Feedback.builder()
                .message(message)
                .role(role.toUkr())
                .branch(branch)
                .sentiment(analysis.sentiment().toUkr())
                .severity(analysis.severity())
                .advice(analysis.advice())
                .createdAt(LocalDate.now())
                .build();


        Feedback saved = feedbackRepository.save(feedback);


        try {
            googleSheetsService.appendFeedback(saved);
            // googleDocsService.appendFeedback(saved);
        } catch (Exception e) {
            System.err.println("Не вдалося записати у Google Sheets: " + e.getMessage());
        }


        if(feedback.getSeverity() >= 4) {
            try {
                String trelloName = "[" + feedback.getBranch() + "] [" + feedback.getRole() + "] [" + feedback.getSeverity() + "]";
                String trelloDesc = "Повідомлення: " + feedback.getMessage() +
                        "\nПорада: " + feedback.getAdvice() +
                        "\nДата: " + feedback.getCreatedAt();

                trelloService.createCard(trelloName, trelloDesc);
            } catch (Exception e) {
                System.err.println("Не вдалося записати Trello карту: " + e.getMessage());
            }
        }

        return saved;
    }


    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }


}
