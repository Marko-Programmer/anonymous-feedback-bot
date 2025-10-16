package com.marko.anonymous_feedback_bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {
    MECHANIC, ELECTRICIAN, MANAGER, ADMIN;

    public String toUkr() {
        return switch (this) {
            case MECHANIC -> "Механік";
            case ELECTRICIAN -> "Електрик";
            case MANAGER -> "Менеджер";
            case ADMIN -> "Адмін";
        };
    }
}
