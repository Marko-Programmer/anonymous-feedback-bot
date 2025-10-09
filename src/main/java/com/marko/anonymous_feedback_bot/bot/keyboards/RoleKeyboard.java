package com.marko.anonymous_feedback_bot.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class RoleKeyboard {

    public static ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();


        KeyboardRow row = new KeyboardRow();
        row.add("Механік");
        row.add("Електрик");
        row.add("Менеджер");
        keyboard.add(row);


        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Назад");
        keyboard.add(backRow);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}
