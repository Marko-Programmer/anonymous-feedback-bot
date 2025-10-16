package com.marko.anonymous_feedback_bot.bot;

import com.marko.anonymous_feedback_bot.bot.keyboards.*;
import com.marko.anonymous_feedback_bot.bot.session.RegistrationSession;
import com.marko.anonymous_feedback_bot.model.Feedback;
import com.marko.anonymous_feedback_bot.model.Role;
import com.marko.anonymous_feedback_bot.model.User;
import com.marko.anonymous_feedback_bot.model.UserFilters;
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

import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FeedbackBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final FeedbackService feedbackService;

    private final Map<Long, RegistrationSession> registrationSessions = new HashMap<>();
    private final Map<Long, Boolean> awaitingFeedback = new HashMap<>();
    private final Map<Long, Boolean> inAdminPanel = new HashMap<>();
    private final Map<Long, Boolean> inFilterPanel = new HashMap<>();

    private final Map<Long, UserFilters> userFiltersMap = new HashMap<>();




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




        if (inFilterPanel.getOrDefault(telegramId, false)) {
            UserFilters filters = userFiltersMap.computeIfAbsent(telegramId, k -> new UserFilters());
            String cleanText = text.replace(" ✅", "");

            // 🔹 якщо натиснуто кнопку ролі
            if (filters.roleFilters.containsKey(cleanText)) {
                handleRoleFilter(telegramId, chatId, text);
                return;
            }

            // 🔹 якщо натиснуто кнопку філії
            if (filters.branchFilters.containsKey(cleanText)) {
                handleBranchFilter(telegramId, chatId, text);
                return;
            }

            // 🔹 якщо натиснуто кнопку філії
            if (filters.severityFilters.containsKey(cleanText)) {
                handleSeverityFilter(telegramId, chatId, text);
                return;
            }

            filterFeedbacks(chatId, telegramId, text);
            return;
        }



        if (inAdminPanel.getOrDefault(telegramId, false)) {
            handleAdminPanel(chatId, telegramId, text);
            return;
        }

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





    // Логіка реєстрації

    private static final Map<String, Role> ROLE_MAP = Map.of(
            "Механік", Role.MECHANIC,
            "Електрик", Role.ELECTRICIAN,
            "Менеджер", Role.MANAGER,
            "Адмін", Role.ADMIN
    );

    private void handleRegistration(Long telegramId, String text, Long chatId) {
        RegistrationSession session = registrationSessions.computeIfAbsent(telegramId, k -> new RegistrationSession());

    // Кнопка назад
        if ("⬅️ Назад".equals(text)) {
            if (session.isAwaitingAdminPassword() || (session.getRole() != null && session.getBranch() == null)) {
                session.setRole(null);
                session.setAwaitingAdminPassword(false);
                sendMessageKeyboard(chatId, "Оберіть вашу роль:", RoleSelectionKeyboard.getKeyboard());
            } else {
                registrationSessions.remove(telegramId);
                sendMessageKeyboard(chatId, "Повертаємось до головного меню.", MenuKeyboard.getKeyboard());
            }
            return;
        }


    // Якщо чекаємо пароль
        if (session.isAwaitingAdminPassword()) {
            if (adminPassword.equals(text)) {
                session.setAwaitingAdminPassword(false);
                registrationSessions.remove(telegramId);
                inAdminPanel.put(telegramId, true);
                handleAdminPanel(chatId, telegramId, "");
            } else {
                sendMessageKeyboard(chatId, "Невірний пароль. Спробуйте ще раз.", BackKeyboard.getKeyboard());
            }
            return;
        }

//  Вибір ролі
        if (session.getRole() == null) {
            Role selectedRole = ROLE_MAP.get(text);
            if (selectedRole != null) {
                session.setRole(selectedRole);

                if (selectedRole == Role.ADMIN) {
                    session.setAwaitingAdminPassword(true);
                    sendMessageKeyboard(chatId, "Введіть пароль адміністратора:", BackKeyboard.getKeyboard());
                } else {
                    sendMessageKeyboard(chatId, "Дякую! Тепер оберіть філію:", BranchKeyboard.getKeyboard());
                }
            } else {
                sendMessageKeyboard(chatId, "Оберіть вашу роль:", RoleSelectionKeyboard.getKeyboard());
            }
            return;
        }

//   Якщо філія ще не обрана
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







    // логіка адмін-панелі

    private void handleAdminPanel(Long chatId, Long telegramId, String text) {
        UserFilters filters = userFiltersMap.get(telegramId);

        switch (text) {

            case "Знайти відгуки":
                findFeedbacks(telegramId, chatId);
                break;

            case "Фільтрувати":
                filterFeedbacks(telegramId, chatId, "");
                inFilterPanel.put(telegramId, true);
                break;


            case "🏠 Головне меню":
                sendMessageKeyboard(chatId, "Повертаємось у головне меню:", MenuKeyboard.getKeyboard());
                if (filters != null) {
                    filters.roleFilters.replaceAll((k, v) -> true);
                    filters.branchFilters.replaceAll((k, v) -> true);
                    filters.severityFilters.replaceAll((k, v) -> true);
                }
                inAdminPanel.remove(telegramId);
                inFilterPanel.remove(telegramId);
                break;

            default:
                sendMessageKeyboard(chatId, "Оберіть дію:", AdminPanelKeyboard.getKeyboard());
                break;
        }
    }




    // Пошук відгуків

    void findFeedbacks(Long telegramId, Long chatId) {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();

        if (feedbacks.isEmpty()) {
            sendMessage(chatId, "Відгуків поки немає...");
            return;
        }

        // Отримуємо фільтри користувача (якщо є)
        UserFilters filters = userFiltersMap.getOrDefault(telegramId, new UserFilters());

        // 🔹 Фільтрація за роллю
        feedbacks = feedbacks.stream()
                .filter(fb -> filters.roleFilters.getOrDefault(fb.getRole().toString(), true))
                .filter(fb -> filters.branchFilters.getOrDefault(fb.getBranch(), true))
                .filter(fb -> filters.severityFilters.getOrDefault(String.valueOf(fb.getSeverity()), true))
                .toList();

        if (feedbacks.isEmpty()) {
            sendMessage(chatId, "Немає відгуків, що відповідають активним фільтрам.");
            sendMessageKeyboard(chatId, "Оберіть дію:", AdminPanelKeyboard.getKeyboard());
            return;
        }

        StringBuilder response = new StringBuilder("*Список відгуків:*\n\n");

        for (Feedback feedback : feedbacks) {
            response.append("*[")
                    .append(feedback.getBranch())
                    .append(", ")
                    .append(feedback.getRole())
                    .append("] (")
                    .append(feedback.getSeverity())
                    .append("):* ")
                    .append("\n")
                    .append(feedback.getMessage())
                    .append("\n")
                    .append(feedback.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append("\n\n");
        }

        sendMessageWithMarkdown(chatId, response.toString());
        sendMessageKeyboard(chatId, "\nОберіть дію:", AdminPanelKeyboard.getKeyboard());
    }




    // Фільтрування відгуків

    private void filterFeedbacks(Long chatId, Long telegramId, String text) {
        UserFilters filters = userFiltersMap.get(telegramId);


        switch (text) {

            case "За філією":
                inFilterPanel.put(telegramId, true);
                sendMessageKeyboard(chatId, "Оберіть філію:", BranchFilterKeyboard.getKeyboard(filters));
                break;

            case "За роллю":
                inFilterPanel.put(telegramId, true);
                sendMessageKeyboard(chatId, "Оберіть роль:", RoleFilterKeyboard.getKeyboard(filters));
                break;

            case "За критичністю":
                inFilterPanel.put(telegramId, true);
                sendMessageKeyboard(chatId, "Оберіть рівень критичності:", SeverityFilterKeyboard.getKeyboard(filters));
                break;

            case "Скинути фільтри":

                if (filters != null) {
                    filters.roleFilters.replaceAll((k, v) -> true);
                    filters.branchFilters.replaceAll((k, v) -> true);
                    filters.severityFilters.replaceAll((k, v) -> true);
                }
                inFilterPanel.remove(telegramId);

                sendMessage(chatId, "Фільтри скинуто");
                sendMessageKeyboard(chatId, "Оберіть дію:", AdminPanelKeyboard.getKeyboard());
                break;

            case "Назад":
                inFilterPanel.remove(telegramId);
                sendMessageKeyboard(chatId, "\nПовертаємось:", AdminPanelKeyboard.getKeyboard());
                break;

            default:
                sendMessageKeyboard(chatId, "Оберіть тип фільтру:", FilterChooseKeyboard.getKeyboard());
                break;
        }
    }

    // Логіка фільтра ролі

    private void handleRoleFilter(Long telegramId, Long chatId, String text) {
        UserFilters filters = userFiltersMap.get(telegramId);
        if (filters == null) {
            filters = new UserFilters();
            userFiltersMap.put(telegramId, filters);
        }

        if ("⬅️ Назад".equals(text)) {
            sendMessageKeyboard(chatId, "Повертаємось:", AdminPanelKeyboard.getKeyboard());
            return;
        }

        String roleKey = text.replace(" ✅", "");
        if (filters.roleFilters.containsKey(roleKey)) {
            boolean currentState = filters.roleFilters.get(roleKey);
            filters.roleFilters.put(roleKey, !currentState);

            if (currentState) {
                sendMessage(chatId, "Роль " + roleKey + " вилучена з пошуку.");
            } else {
                sendMessage(chatId, "Роль " + roleKey + " знову враховується в пошуку.");
            }
        }

        sendMessageKeyboard(chatId, "Оберіть роль:", RoleFilterKeyboard.getKeyboard(filters));
 }


    // 🔹 Логіка фільтрації за філією
    private void handleBranchFilter(Long telegramId, Long chatId, String text) {
        UserFilters filters = userFiltersMap.get(telegramId);
        if (filters == null) {
            filters = new UserFilters();
            userFiltersMap.put(telegramId, filters);
        }

        if ("⬅️ Назад".equals(text)) {
            inFilterPanel.remove(telegramId);
            sendMessageKeyboard(chatId, "Повертаємось:", AdminPanelKeyboard.getKeyboard());
            return;
        }


        String branchKey = text.replace(" ✅", "");
        if (filters.branchFilters.containsKey(branchKey)) {
            boolean currentState = filters.branchFilters.get(branchKey);
            filters.branchFilters.put(branchKey, !currentState);

            if (currentState) {
                sendMessage(chatId, branchKey + " вилучена з пошуку.");
            } else {
                sendMessage(chatId, branchKey + " знову враховується в пошуку.");
            }
        }

        sendMessageKeyboard(chatId, "Оберіть філію:", BranchFilterKeyboard.getKeyboard(filters));
    }


    private void handleSeverityFilter(Long telegramId, Long chatId, String text) {
        UserFilters filters = userFiltersMap.get(telegramId);
        if (filters == null) {
            filters = new UserFilters();
            userFiltersMap.put(telegramId, filters);
        }

        if ("⬅️ Назад".equals(text)) {
            inFilterPanel.remove(telegramId);
            sendMessageKeyboard(chatId, "Повертаємось:", AdminPanelKeyboard.getKeyboard());
            return;
        }


        String severityKey = text.replace(" ✅", "");
        if (filters.severityFilters.containsKey(severityKey)) {
            boolean currentState = filters.severityFilters.get(severityKey);
            filters.severityFilters.put(severityKey, !currentState); // перемикаємо стан

            if (currentState) {
                sendMessage(chatId, "Рівень критичності " + severityKey + " вилучений з пошуку.");
            } else {
                sendMessage(chatId, "Рівень критичності " + severityKey + " знову враховується в пошуку.");
            }
        }

        sendMessageKeyboard(chatId, "Оберіть рівень критичності:", SeverityFilterKeyboard.getKeyboard(filters));
    }



    // логіка відправки повідомлень

    public void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }


    public void sendMessageKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage message = new SendMessage(chatId.toString(), text);
            message.setReplyMarkup(keyboard);
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }


    public void sendMessageWithMarkdown(Long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("Markdown")
                    .build();
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Помилка при надсиланні Markdown-повідомлення: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }
}