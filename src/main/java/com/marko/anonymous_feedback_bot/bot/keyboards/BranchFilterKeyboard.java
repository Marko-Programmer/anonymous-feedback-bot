package com.marko.anonymous_feedback_bot.bot.keyboards;

import com.marko.anonymous_feedback_bot.model.UserFilters;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BranchFilterKeyboard {

    public static ReplyKeyboardMarkup getKeyboard(UserFilters filters) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // 🔹 Отримуємо стабільний порядок філій
        List<String> branches = filters.branchFilters.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < branches.size(); i++) {
            String branch = branches.get(i);
            boolean active = filters.branchFilters.get(branch);
            currentRow.add(active ? branch + " ✅" : branch);

            // кожні 3 кнопки — новий ряд
            if ((i + 1) % 3 == 0) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        // додаємо останній ряд, якщо не порожній
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        // 🔹 ряд з "Назад"
        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Назад");
        keyboard.add(backRow);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
