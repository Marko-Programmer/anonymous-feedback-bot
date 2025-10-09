package com.marko.anonymous_feedback_bot.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class BranchKeyboard {



    public static ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Припустимо, у нас 5 філій
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Філія 1");
        row1.add("Філія 2");
        row1.add("Філія 3");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Філія 4");
        row2.add("Філія 5");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
