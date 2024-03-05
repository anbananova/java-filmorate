package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final List<User> users = new ArrayList<>();

    @GetMapping
    public List<User> getUsers() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return users;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);

        user.setId(users.size() + 1);

        if (users.contains(user)) {
            throw new UserAlreadyExistException("Пользователь уже был добавлен: " + user);
        }

        log.debug("Текущий пользователь: {}", user);
        users.add(user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        validateUser(user);

        if (users.contains(user)) {
            users.remove(user);
        } else {
            throw new ValidationException("Такого пользователя не существует: " + user);
        }

        log.debug("Текущий пользователь: {}", user);
        users.add(user);
        return user;
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("У пользователя пробелы в логине: " + user.getLogin());
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем: " + user.getBirthday());
        }
    }
}
