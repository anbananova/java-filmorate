package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("Db") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getAll();
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

    public User create(@Valid User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    public User update(@Valid User user) {
        validateUser(user);
        return userStorage.update(user);
    }


    public User getUserById(Integer userId) {
        return userStorage.getUser(userId).orElse(null);
    }


    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId).orElse(null);
        User friend = userStorage.getUser(friendId).orElse(null);

        if (user != null && friend != null) {
            userStorage.addFriend(user, friend);
        }

        log.debug("addFriend Текущий пользователь: {}", user);
        log.debug("addFriend Друг для добавления: {}", friend);
        return user;
    }

    public User removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId).orElse(null);
        User friend = userStorage.getUser(friendId).orElse(null);
        if (user != null && friend != null) {
            userStorage.removeFriend(user, friend);
        }

        log.debug("removeFriend Текущий пользователь: {}", user);
        log.debug("removeFriend Друг для удаления: {}", friend);
        return user;
    }

    public List<User> getUserFriends(Integer userId) {
        User user = userStorage.getUser(userId).orElse(null);
        List<User> userFriends = new ArrayList<>();

        if (user != null) {
            userFriends = userStorage.getUserFriends(user);
            log.debug("getUserFriends Текущий пользователь: {}", user);
            log.debug("getUserFriends Друзья пользователя {}: {}", user.getId(), userFriends.stream().map(User::getId)
                    .collect(Collectors.toList()));
        }
        return userFriends;
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        List<User> userFriends = getUserFriends(userId);
        List<User> otherUserFriends = getUserFriends(otherUserId);

        log.debug("getCommonFriends Текущий пользователь: {}", userStorage.getUser(userId));
        log.debug("getCommonFriends Другой пользователь: {}", userStorage.getUser(otherUserId));

        userFriends.retainAll(otherUserFriends);
        return userFriends;
    }
}
