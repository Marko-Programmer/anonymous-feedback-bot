package com.marko.anonymous_feedback_bot.bot.keyboards;

import com.marko.anonymous_feedback_bot.model.UserFilters;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeverityFilterKeyboard {

    public static ReplyKeyboardMarkup getKeyboard(UserFilters filters) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        List<String> severities = filters.severityFilters.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < severities.size(); i++) {
            String severity = severities.get(i);
            boolean active = filters.severityFilters.getOrDefault(severity, true);
            currentRow.add(active ? severity + " ✅" : severity);

            if ((i + 1) % 3 == 0) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Назад");
        keyboard.add(backRow);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
