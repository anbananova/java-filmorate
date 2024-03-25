package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        // Подготавливаем данные для теста
        User newUser = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.add(newUser);

        // вызываем тестируемый метод
        User savedUser = userStorage.getUser(newUser.getId()).orElse(null);

        // проверяем утверждения
        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(newUser);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testUpdateUser() {
        // Подготавливаем данные для теста
        User newUser = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.add(newUser);

        User updatedUser = new User(newUser.getId(), "user@emailUPDATES.ru", "vanya123", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.update(updatedUser);

        // вызываем тестируемый метод
        User savedUser = userStorage.getUser(updatedUser.getId()).orElse(null);

        // проверяем утверждения
        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(updatedUser);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testGetAllUsers() {
        // Подготавливаем данные для теста
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User newUser1 = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        userStorage.add(newUser1);

        User newUser2 = new User(2, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        // вызываем тестируемый метод
        List<User> savedUsers = userStorage.getAll();

        // проверяем утверждения
        assertThat(savedUsers)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newUser1, newUser2));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testAddFriend() {
        // Подготавливаем данные для теста
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User newUser1 = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        userStorage.add(newUser1);

        User newUser2 = new User(2, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        userStorage.addFriend(newUser1, newUser2);
        // вызываем тестируемый метод
        List<User> savedUsers = userStorage.getUserFriends(newUser1);

        // проверяем утверждения
        assertThat(savedUsers)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newUser2));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testRemoveFriend() {
        // Подготавливаем данные для теста
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User newUser1 = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        userStorage.add(newUser1);

        User newUser2 = new User(2, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        User newUser3 = new User(3, "user@email333.ru", "vanya12345678", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser3);

        userStorage.addFriend(newUser1, newUser2);
        userStorage.addFriend(newUser1, newUser3);

        userStorage.removeFriend(newUser1, newUser2);
        // вызываем тестируемый метод
        List<User> savedUsers = userStorage.getUserFriends(newUser1);

        // проверяем утверждения
        assertThat(savedUsers)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newUser3));        // и сохраненного пользователя - совпадают
    }
}