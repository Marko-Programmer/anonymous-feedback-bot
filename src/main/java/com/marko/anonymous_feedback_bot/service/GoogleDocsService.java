//package com.marko.anonymous_feedback_bot.service;
//
//
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.marko.anonymous_feedback_bot.model.Feedback;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.services.docs.v1.Docs;
//import com.google.api.services.docs.v1.model.*;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.FileInputStream;
//import java.time.format.DateTimeFormatter;
//import java.util.Collections;
//
//@Service
//public class GoogleDocsService {
//
//
//    private static final String APPLICATION_NAME = "AnonymousFeedbackBot";
//
//    @Value("${google.api.credentials.path}")
//    private String credentialsFilePath;
//
//    @Value("${google.document.id}")
//    private String documentId;
//
//    private Docs docsService;
//
//
//    public GoogleDocsService() {
//    }
//
//    @PostConstruct
//    public void init() {
//        try {
//            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
//                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/documents"));
//
//            docsService = new Docs.Builder(
//                    GoogleNetHttpTransport.newTrustedTransport(),
//                    JacksonFactory.getDefaultInstance(),
//                    new HttpCredentialsAdapter(credentials)
//            ).setApplicationName(APPLICATION_NAME).build();
//
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//        }
//    }
//
//    public void appendFeedback(Feedback feedback) throws Exception {
//        if (docsService == null) throw new IllegalStateException("Docs service not initialized");
//
//        String formattedFeedback = formatFeedback(feedback);
//
//        // Додаємо текст у кінець документа
//        Request request = new Request()
//                .setInsertText(new InsertTextRequest()
//                        .setText(formattedFeedback + "\n")
//                        .setEndOfSegmentLocation(new EndOfSegmentLocation()
//                                .setSegmentId("")));
//
//        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(Collections.singletonList(request));
//
//        docsService.documents().batchUpdate(documentId, body).execute();
//    }
//
//    private String formatFeedback(Feedback feedback) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        return String.format("[%s] [%s] [%s] [%s] [%d] [%s]",
//                feedback.getCreatedAt().format(formatter),
//                feedback.getBranch(),
//                feedback.getRole(),
//                feedback.getSentiment(),
//                feedback.getSeverity(),
//                feedback.getMessage());
//    }
//
//}
