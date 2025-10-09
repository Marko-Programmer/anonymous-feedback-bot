package com.marko.anonymous_feedback_bot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "feedbacks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Повідомлення не може бути порожнім")
    @Size(max = 1000, message = "Повідомлення занадто довге")
    private String message;

    private String advice;

    @NotBlank(message = "Роль не може бути порожньою")
    private String role;

    @NotBlank(message = "Філія не може бути порожньою")
    private String branch;

    @NotNull(message = "Настрій обов'язковий")
    private String sentiment;

    @Min(value = 1, message = "Критичність має бути від 1 до 5")
    @Max(value = 5, message = "Критичність має бути від 1 до 5")
    private int severity;


    @NotNull
    private LocalDate createdAt;


}
