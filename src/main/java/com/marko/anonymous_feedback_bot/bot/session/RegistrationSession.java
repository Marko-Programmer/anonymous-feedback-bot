package com.marko.anonymous_feedback_bot.bot.session;

import com.marko.anonymous_feedback_bot.model.Role;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RegistrationSession {
    private Role role;
    private String branch;
    private boolean awaitingAdminPassword;
    private final Map<Long, Boolean> inAdminPanel = new HashMap<>();
}
