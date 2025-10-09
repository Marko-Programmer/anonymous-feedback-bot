package com.marko.anonymous_feedback_bot.bot;

import com.marko.anonymous_feedback_bot.bot.keyboards.*;
import com.marko.anonymous_feedback_bot.bot.session.RegistrationSession;
import com.marko.anonymous_feedback_bot.model.Role;
import com.marko.anonymous_feedback_bot.model.User;
import com.marko.anonymous_feedback_bot.repository.UserRepository;
import com.marko.anonymous_feedback_bot.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FeedbackBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserRepository userRepository;
    private final FeedbackService feedbackService;

    private final Map<Long, RegistrationSession> registrationSessions = new HashMap<>();
    private final Map<Long, Boolean> awaitingFeedback = new HashMap<>();



    @Override
    public void onRegister() {
        try {
            this.execute(new SetMyCommands(
                    List.of(new BotCommand("/start", "Розпочати роботу з ботом")),
                    new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }



    // Основна логіка бота

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long telegramId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();


        if (registrationSessions.containsKey(telegramId)) {
            handleRegistration(telegramId, text, chatId);
            return;
        }


        if (awaitingFeedback.getOrDefault(telegramId, false)) {
            if ("⬅️ Назад".equals(text)) {
                awaitingFeedback.remove(telegramId);
                sendMessageKeyboard(chatId, "Повертаємось до головного меню:", MenuKeyboard.getKeyboard());
                return;
            }

            User user = userRepository.findById(telegramId).orElse(null);
            if (user != null) {
                sendMessage(chatId, "Йде обробка повідомлення...");
                handleFeedback(text, user.getRole(), user.getBranch(), user.getTelegramId(), chatId);
                awaitingFeedback.remove(telegramId);
            } else {
                sendMessage(chatId, "Спочатку зареєструйтесь через меню!");
            }
            return;
        }


        Optional<User> existingUser = userRepository.findById(telegramId);


        switch (text) {
            case "/start":
                sendMessageKeyboard(chatId, "Вітаю! Оберіть дію:", MenuKeyboard.getKeyboard());
                break;

            case "Реєстрація / Зміна ролі та філії":
                registrationSessions.remove(telegramId);
                handleRegistration(telegramId, "", chatId);
                break;

            case "Надіслати відгук":
                if (existingUser.isPresent()) {
                    sendMessageKeyboard(chatId, "Введіть ваш відгук:", BackKeyboard.getKeyboard());
                    awaitingFeedback.put(telegramId, true);
                } else {
                    sendMessage(chatId, "Спочатку зареєструйтесь через меню!");
                }
                break;

            default:
                sendMessageKeyboard(chatId, "Оберіть одну з опцій нижче 👇", MenuKeyboard.getKeyboard());
                break;
        }
    }



    // Логіка реєстрації

    private static final Map<String, Role> ROLE_MAP = Map.of(
            "Механік", Role.MECHANIC,
            "Електрик", Role.ELECTRICIAN,
            "Менеджер", Role.MANAGER
    );

    private void handleRegistration(Long telegramId, String text, Long chatId) {
        RegistrationSession session = registrationSessions.computeIfAbsent(telegramId, k -> new RegistrationSession());


        if ("⬅️ Назад".equals(text)) {
            if (session.getRole() != null && session.getBranch() == null) {
                session.setRole(null);
                sendMessageKeyboard(chatId, "Оберіть вашу роль:", RoleKeyboard.getKeyboard());
                return;
            } else {
                registrationSessions.remove(telegramId);
                sendMessageKeyboard(chatId, "Повертаємось до головного меню.", MenuKeyboard.getKeyboard());
                return;
            }
        }


        if (session.getRole() == null) {
            Role selectedRole = ROLE_MAP.get(text);
            if (selectedRole != null) {
                session.setRole(selectedRole);
                sendMessageKeyboard(chatId, "Дякую! Тепер оберіть філію:", BranchKeyboard.getKeyboard());
            } else {
                sendMessageKeyboard(chatId, "Оберіть вашу роль:", RoleKeyboard.getKeyboard());
            }
            return;
        }


        if (session.getBranch() == null) {
            session.setBranch(text);

            User user = userRepository.findById(telegramId).orElse(new User());
            user.setTelegramId(telegramId);
            user.setRole(session.getRole());
            user.setBranch(session.getBranch());
            userRepository.save(user);

            registrationSessions.remove(telegramId);
            sendMessageKeyboard(chatId, "Дані оновлено! Тепер можете надсилати відгуки.", MenuKeyboard.getKeyboard());
        }
    }




    // Логіка обробки відгуку

    private void handleFeedback(String message, Role role, String branch, Long telegramId, Long chatId) {
        try {
            feedbackService.processAndSaveFeedback(message, role, branch);
            sendMessageKeyboard(chatId,
                    "Ваш відгук отримано та проаналізовано. Дякуємо за допомогу у покращенні сервісу!",
                    BackToMenuKeyboard.getKeyboard());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            sendMessage(chatId, "Сталася помилка при обробці відгуку. Спробуйте пізніше.");
        }
    }





    private void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendMessageKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }




    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }
}