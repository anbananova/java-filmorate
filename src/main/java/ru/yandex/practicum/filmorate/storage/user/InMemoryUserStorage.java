package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
@Qualifier("InMemory")
public class InMemoryUserStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();
    private final Map<List<Integer>, FriendshipStatus> friendships = new HashMap<>();

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
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(users.stream().filter(u -> id == u.getId()).findFirst()
                .orElseThrow(() -> new NotFoundException("Такого пользователя с id = " + id + " не существует.")));
    }

    @Override
    public void addFriend(User user, User friend) {
        if (!friendships.containsKey(List.of(user.getId(), friend.getId()))) {
            friendships.put(List.of(user.getId(), friend.getId()), FriendshipStatus.UNACCEPTED);
        }
        if (friendships.containsKey(List.of(friend.getId(), user.getId()))) {
            friendships.put(List.of(user.getId(), friend.getId()), FriendshipStatus.ACCEPTED);
            friendships.put(List.of(friend.getId(), user.getId()), FriendshipStatus.ACCEPTED);
        }
    }

    @Override
    public void removeFriend(User user, User friend) {
        if (friendships.containsKey(List.of(friend.getId(), user.getId()))) {
            friendships.put(List.of(friend.getId(), user.getId()), FriendshipStatus.UNACCEPTED);
        }
        friendships.remove(List.of(user.getId(), friend.getId()));
    }

    @Override
    public List<User> getUserFriends(User user) {
        List<User> friends = new ArrayList<>();

        for (List<Integer> couple : friendships.keySet()) {
            if (couple.get(0) == user.getId()) {
                int friendId = couple.get(1);
                User friend = getUser(friendId).orElse(null);
                friends.add(friend);
            }
        }
        return friends;
    }
}
