package org.vandsoft.telegrambot.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vandsoft.telegrambot.model.Task;
import org.vandsoft.telegrambot.model.User;
import org.vandsoft.telegrambot.service.TaskService;
import org.vandsoft.telegrambot.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@PropertySource("classpath:application.properties")
public class ToDoBot extends TelegramLongPollingBot {
    final private UserService userService;
    final private TaskService taskService;
    final private String botName;

    public ToDoBot(UserService userService, TaskService taskService, @Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super(botToken);
        this.userService = userService;
        this.taskService = taskService;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) {
            return;
        }

        Long chatId;
        String text;
        User user = null;

        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            text = update.getCallbackQuery().getData();
            user = userService.registerUser(chatId);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            text = update.getMessage().getText();
            user = userService.registerUser(chatId);
        } else {
            return;
        }

        try {
            switch (text.split(" ")[0]) {
                case "/start": comandStart(chatId); comandHelp(chatId); break;
                case "/add": comandAdd(chatId, text, user); break;
                case "/list": comandList(chatId, user); break;
                case "/done": comandDone(chatId, text); break;
                case "/delete": comandDelete(chatId, text); comandList(chatId, user); break;
                default: comandHelp(chatId);
            }
        }catch (Exception e) {
            reply(chatId, "Error: " + e.getMessage());
        }
    }

    private void comandStart(Long chatId) {
        reply(chatId, "Welcome to ToDo Bot!");
    }

    private void comandAdd(Long chatId, String text, User user) {
        reply(chatId, "You are about to add a new task.");
        String taskText = text.replaceFirst("/add", "").trim();
        if (taskText.isEmpty()) {
            reply(chatId, "Please enter a task.");
            return;
        }
        Task task = taskService.addTask(user, taskText);
        reply(chatId, "Task #"+task.getId()+" added: " + taskText);
        return;
    }

    private void comandList(Long chatId, User user) {
        List<Task> tasks = taskService.getTasks(user);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (tasks.isEmpty()) {
            reply(chatId, "No tasks found.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Your tasks:\n");
        for (Task task : tasks) {
            InlineKeyboardButton button1 = InlineKeyboardButton.builder().text("Mark task #" + task.getId() + " as done").callbackData("/done " + task.getId()).build();
            InlineKeyboardButton button2 = InlineKeyboardButton.builder().text("Delete task #"+ task.getId()).callbackData("/delete "+task.getId()).build();
            rows.add(List.of(button1, button2));

         //   rows.add(Collections.singletonList(InlineKeyboardButton.builder().text("Mark task #" + task.getId() + " as done").callbackData("/done " + task.getId()).build()));
            sb.append('#').append(task.getId()).append(' ').append('[').append(task.getStatus()).append(']').append(' ').append(task.getText()).append('\n');
        }
        replyWithKeyboard(chatId, sb.toString(), rows);
    }

    private void comandDone(Long chatId,String text) {
        String taskId = text.replaceFirst("/done", "").trim();
        if (taskId.isEmpty()) {
            reply(chatId, "Please enter a task ID.");
            return;
        }
        Long id = parseId(taskId);
        if (id == null) {
            reply(chatId, "Please enter a correct task ID.");
            return;
        }
        Optional<Task> updatedTask = taskService.markDone(id);
        if (updatedTask.isPresent()) {
            reply(chatId, "Task #"+id+" marked as done.");
        }else {
            reply(chatId, "Task #"+id+" not found.");
        }
    }

    private void comandDelete(Long chatId, String text) {
        String taskId = text.replaceFirst("/delete", "").trim();
        if (taskId.isEmpty()) {
            reply(chatId, "Please enter a task ID.");
            return;
        }
        Long id = parseId(taskId);
        if (id == null) {
            reply(chatId, "Please enter a correct task ID.");
            return;
        }
        taskService.deleteTask(id);
    }

    private void comandHelp(Long chatId) {
        reply(chatId, "You can use next commands:\n" +
                "/start - registration\n" +
                "/add <text> - add task\n" +
                "/list - show tasks\n" +
                "/done <id> - mark task as done\n" +
                "/delete <id> - delete");
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    private void reply(Long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {e.printStackTrace();}
    }

    private void replyWithKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> keyboardsRow) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(keyboardsRow);
        msg.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(msg);
        } catch (TelegramApiException e) {e.printStackTrace();}
    }

    private Long parseId(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
