package org.vandsoft.telegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vandsoft.telegrambot.model.Status;
import org.vandsoft.telegrambot.model.Task;
import org.vandsoft.telegrambot.model.User;
import org.vandsoft.telegrambot.repository.TaskRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    final private TaskRepository taskRepository;

    public Task addTask(User user, String text) {
        Task task = new Task();
        task.setUser(user);
        task.setText(text);
        task.setCreatedAt(Instant.now());
        task.setStatus(Status.IN_PROGRESS);
        return taskRepository.save(task);
    }

    public List<Task> getTasks(User user) {
        return taskRepository.findByUserOrderByCreatedAt(user);
    }

    public Optional<Task> markDone(Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            Task taskToMark = task.get();
            taskToMark.setStatus(Status.DONE);
            return Optional.of(taskRepository.save(taskToMark));
        }
        return task;
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
