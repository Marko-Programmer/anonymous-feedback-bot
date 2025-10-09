package com.marko.anonymous_feedback_bot.config;

import com.marko.anonymous_feedback_bot.bot.FeedbackBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    private final FeedbackBot feedbackBot;

    public BotConfig(FeedbackBot feedbackBot) {
        this.feedbackBot = feedbackBot;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(feedbackBot);
        return botsApi;
    }
}
