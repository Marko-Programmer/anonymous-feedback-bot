package com.marko.anonymous_feedback_bot.repository;

import com.marko.anonymous_feedback_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
