package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") Integer userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable("id") Integer id) {
        return userService.getUserFriends(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Integer userId,
                          @PathVariable("friendId") Integer friendId) {
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable("id") Integer userId,
                             @PathVariable("friendId") Integer friendId) {
        return userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Integer userId,
                                       @PathVariable("otherId") Integer otherUserId) {
        return userService.getCommonFriends(userId, otherUserId);
    }
}
