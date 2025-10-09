package com.marko.anonymous_feedback_bot.repository;

import com.marko.anonymous_feedback_bot.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {


}
