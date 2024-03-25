package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("Db")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday()
        );

        setUserId(user);

        log.debug("Текущий пользователь: {}", user);
        return user;
    }

    @Override
    public void remove(User user) {
        if (getUser(user.getId()).isPresent()) {
            jdbcTemplate.update(
                    "DELETE FROM users WHERE id = ?", user.getId()
            );
        } else {
            throw new NotFoundException("Такого пользователя не существует: " + user);
        }
    }

    @Override
    public User update(User user) {
        if (getUser(user.getId()).isPresent()) {
            jdbcTemplate.update(
                    "UPDATE users SET email = ? WHERE id = ?", user.getEmail(), user.getId()
            );
            jdbcTemplate.update(
                    "UPDATE users SET login = ? WHERE id = ?", user.getLogin(), user.getId()
            );
            jdbcTemplate.update(
                    "UPDATE users SET name = ? WHERE id = ?", user.getName(), user.getId()
            );
            jdbcTemplate.update(
                    "UPDATE users SET birthday = ? WHERE id = ?", user.getBirthday(), user.getId()
            );
        } else {
            throw new NotFoundException("Такого пользователя не существует: " + user);
        }

        log.debug("Текущий пользователь: {}", user);

        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public Optional<User> getUser(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);

        if (userRows.next()) {
            User user = new User(userRows.getInt("id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate());

            log.info("Найден пользователь: {}", user);

            return Optional.of(user);
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    private void setUserId(User user) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE login = ?", user.getLogin());

        // обрабатываем результат выполнения запроса
        if (userRows.next()) {
            int id = userRows.getInt("id");
            user.setId(id);

            log.info("Найден id - {} для пользователя {}", id, user);

        } else {
            log.info("Пользователь с логином {} не найден.", user.getLogin());
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }

    @Override
    public void addFriend(User user, User friend) {
        SqlRowSet userFriend = jdbcTemplate.queryForRowSet("SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                user.getId(), friend.getId());
        SqlRowSet friendUser = jdbcTemplate.queryForRowSet("SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                friend.getId(), user.getId());

        // добавляем запрос на дружбу user - friend если такой дружбы еще нет
        if (!userFriend.next()) {
            jdbcTemplate.update(
                    "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)", user.getId(), friend.getId()
            );
        }

        // уже есть дружба непринятая friend - user
        if (friendUser.next() &&
                FriendshipStatus.valueOf(friendUser.getString("friendship_status")) == FriendshipStatus.UNACCEPTED) {
            // одобряем дружбу со стороны friend - user
            jdbcTemplate.update(
                    "UPDATE friends SET friendship_status = 'ACCEPTED' WHERE user_id = ? AND friend_id = ?",
                    friend.getId(), user.getId()
            );
            jdbcTemplate.update(
                    "UPDATE friends SET last_updated = NOW() WHERE user_id = ? AND friend_id = ?",
                    friend.getId(), user.getId()
            );
            // одобряем дружбу со стороны user - friend
            jdbcTemplate.update(
                    "UPDATE friends SET friendship_status = 'ACCEPTED' WHERE user_id = ? AND friend_id = ?",
                    user.getId(), friend.getId()
            );
            jdbcTemplate.update(
                    "UPDATE friends SET last_updated = NOW() WHERE user_id = ? AND friend_id = ?",
                    user.getId(), friend.getId()
            );
        }
    }

    @Override
    public void removeFriend(User user, User friend) {
        SqlRowSet userFriend = jdbcTemplate.queryForRowSet("SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                user.getId(), friend.getId());
        SqlRowSet friendUser = jdbcTemplate.queryForRowSet("SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                friend.getId(), user.getId());

        // если была дружба user - friend
        if (userFriend.next()) {
            // удаляем дружбу user - friend
            jdbcTemplate.update(
                    "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                    user.getId(), friend.getId()
            );
        }

        // если была одобренная дружба friend - user
        if (friendUser.next() &&
                FriendshipStatus.valueOf(friendUser.getString("friendship_status")) == FriendshipStatus.ACCEPTED) {
            // убираем подтверждение дружбы friend - user
            jdbcTemplate.update(
                    "UPDATE friends SET friendship_status = 'UNACCEPTED' WHERE user_id = ? AND friend_id = ?",
                    friend.getId(), user.getId()
            );
            jdbcTemplate.update(
                    "UPDATE friends SET last_updated = NOW() WHERE user_id = ? AND friend_id = ?",
                    friend.getId(), user.getId()
            );
        }
    }

    @Override
    public List<User> getUserFriends(User user) {
        String sql = "SELECT * FROM friends WHERE user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> getUser(rs.getInt("friend_id")).orElse(null),
                user.getId());
    }
}
