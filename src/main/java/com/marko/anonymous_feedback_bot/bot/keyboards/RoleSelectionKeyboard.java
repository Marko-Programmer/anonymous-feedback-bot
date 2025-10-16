package com.marko.anonymous_feedback_bot.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class RoleSelectionKeyboard {

    public static ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();


        KeyboardRow row1 = new KeyboardRow();
        row1.add("Механік");
        row1.add("Електрик");
        row1.add("Менеджер");
        keyboard.add(row1);


        KeyboardRow row2 = new KeyboardRow();
        row2.add("Адмін");
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("⬅️ Назад");
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}
