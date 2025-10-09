package com.marko.anonymous_feedback_bot.bot.session;

import com.marko.anonymous_feedback_bot.model.Role;
import lombok.Data;

@Data
public class RegistrationSession {
    private Role role;
    private String branch;
}
