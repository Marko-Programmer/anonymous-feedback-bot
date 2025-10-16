package com.marko.anonymous_feedback_bot.bot.keyboards;

import com.marko.anonymous_feedback_bot.model.UserFilters;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class RoleFilterKeyboard {

    public static ReplyKeyboardMarkup getKeyboard(UserFilters filters) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        for (String role : filters.roleFilters.keySet()) {
            boolean active = filters.roleFilters.get(role);
            row1.add(active ? role + " ✅" : role);
        }
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅️ Назад");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
