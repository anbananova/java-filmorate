package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();

    @Override
    public User add(User user) {
        user.setId(users.size() + 1);

        if (users.contains(user)) {
            throw new UserAlreadyExistException("Пользователь уже был добавлен: " + user);
        }

        log.debug("Текущий пользователь: {}", user);
        users.add(user);
        return user;
    }

    @Override
    public void remove(User user) {
        if (users.contains(user)) {
            users.remove(user);
        } else {
            throw new NotFoundException("Такого пользователя не существует: " + user);
        }
    }

    @Override
    public User update(User user) {
        if (users.contains(user)) {
            users.remove(user);
        } else {
            throw new NotFoundException("Такого пользователя не существует: " + user);
        }

        log.debug("Текущий пользователь: {}", user);
        users.add(user);
        return user;
    }

    @Override
    public List<User> getAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return users;
    }

    @Override
    public User getUser(int id) {
        return users.stream().filter(u -> id == u.getId()).findFirst()
                .orElseThrow(() -> new NotFoundException("Такого пользователя с id = " + id + " не существует."));
    }
}
