package org.vandsoft.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vandsoft.telegrambot.model.Task;
import org.vandsoft.telegrambot.model.User;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAt(User user);
}