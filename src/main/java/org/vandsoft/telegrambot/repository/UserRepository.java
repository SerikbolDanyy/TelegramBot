package org.vandsoft.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vandsoft.telegrambot.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
}