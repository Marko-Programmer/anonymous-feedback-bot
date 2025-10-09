package com.marko.anonymous_feedback_bot.service;

import com.google.api.services.sheets.v4.model.*;
import com.marko.anonymous_feedback_bot.model.Feedback;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    @Value("${google.api.credentials.path}")
    private String credentialsPath;

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private Sheets sheetsService;

    private Sheets getSheetsService() throws Exception {
        if (sheetsService == null) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));

            sheetsService = new Sheets.Builder(
                    com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                    com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("AnonymousFeedbackBot").build();
        }
        return sheetsService;
    }


    public void appendFeedback(Feedback feedback) throws Exception {
        Sheets service = getSheetsService();

        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(
                        Arrays.asList(
                                feedback.getCreatedAt().toString(),
                                feedback.getBranch(),
                                feedback.getRole(),
                                feedback.getSentiment(),
                                feedback.getSeverity(),
                                feedback.getMessage(),
                                feedback.getAdvice() != null ? feedback.getAdvice() : "-"
                        )
                ));

        service.spreadsheets().values()
                .append(spreadsheetId, "Аркуш1!A:G", body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        autoResizeColumns();
    }


    public void autoResizeColumns() throws Exception {
        Sheets service = getSheetsService();
        Integer sheetId = 0;

        List<Request> requests = new ArrayList<>();

        requests.add(new Request().setAutoResizeDimensions(
                new AutoResizeDimensionsRequest()
                        .setDimensions(new DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension("COLUMNS")
                                .setStartIndex(5)
                                .setEndIndex(7)
                        )
        ));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }
}
