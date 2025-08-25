package org.vandsoft.telegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vandsoft.telegrambot.model.User;
import org.vandsoft.telegrambot.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User registerUser(Long telegramId) {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }else {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setCreatedAt(Instant.now());
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
}
