package com.marko.anonymous_feedback_bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Sentiment {
    POSITIVE, NEUTRAL, NEGATIVE;

    public String toUkr() {
        return switch (this) {
            case POSITIVE -> "Позитивний";
            case NEUTRAL -> "Нейтральний";
            case NEGATIVE -> "Негативний";
        };
    }
}
