package com.marko.anonymous_feedback_bot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long telegramId;

    @Enumerated(EnumType.STRING)
    @NotBlank(message = "Роль обов’язкова")
    private Role role;

    @NotBlank(message = "Філія обов’язкова")
    private String branch;

}