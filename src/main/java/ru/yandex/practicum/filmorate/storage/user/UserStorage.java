package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    void remove(User user);

    User update(User user);

    List<User> getAll();

    Optional<User> getUser(int id);

    void addFriend(User user, User friend);

    void removeFriend(User user, User friend);

    List<User> getUserFriends(User user);
}
