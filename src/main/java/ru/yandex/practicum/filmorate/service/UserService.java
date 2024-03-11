package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
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
        return userStorage.getUser(userId);
    }


    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.addFriend((long) friendId);
        friend.addFriend((long) userId);

        log.debug("addFriend Текущий пользователь: {}", user);
        log.debug("addFriend Друг для добавления: {}", friend);
        return user;
    }

    public User removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        try {
            user.removeFriend((long) friendId);
        } catch (NullPointerException e) {
            System.out.println("Нет друзей у пользователя: " + user);
        }
        try {
            friend.removeFriend((long) userId);
        } catch (NullPointerException e) {
            System.out.println("Нет друзей у пользователя: " + friend);
        }

        log.debug("removeFriend Текущий пользователь: {}", user);
        log.debug("removeFriend Друг для удаления: {}", friend);
        return user;
    }

    public List<User> getUserFriends(Integer userId) {
        List<User> userFriends = new ArrayList<>();
        User user = userStorage.getUser(userId);
        Set<Long> userFriendsId = user.getFriends();

        try {
            for (long friendId : userFriendsId) {
                userFriends.add(userStorage.getUser((int) friendId));
            }
        } catch (NullPointerException e) {
            System.out.println("Нет друзей у пользователя: " + user);
        }

        log.debug("getUserFriends Текущий пользователь: {}", user);
        log.debug("getUserFriends Друзья пользователя: {}", userFriendsId);
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
