package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindFilmById() {
        // Подготавливаем данные для теста
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm = new Film(1, "Dune", "The most boring movie ever.",
                LocalDate.of(1990, 1, 1), 666, new Mpa(1, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm);

        // вызываем тестируемый метод
        Film savedFilm = filmStorage.getFilm(newFilm.getId()).orElse(null);

        // проверяем утверждения
        assertThat(savedFilm)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(newFilm);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testUpdateFilm() {
        // Подготавливаем данные для теста
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm = new Film(1, "Dune", "The most boring movie ever.",
                LocalDate.of(1990, 1, 1), 666, new Mpa(1, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm);

        Film updatedFilm = new Film(newFilm.getId(), "Dune 2", "The most boring movie ever 2.",
                LocalDate.of(1990, 1, 1), 6666, new Mpa(4, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(updatedFilm);

        // вызываем тестируемый метод
        Film savedFilm = filmStorage.getFilm(updatedFilm.getId()).orElse(null);

        // проверяем утверждения
        assertThat(savedFilm)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(updatedFilm);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testGetAllFilms() {
        // Подготавливаем данные для теста
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm1 = new Film(1, "Dune", "The most boring movie ever.",
                LocalDate.of(1990, 1, 1), 666, new Mpa(1, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm1);

        Film newFilm2 = new Film(2, "Dune 2", "The most boring movie ever 2.",
                LocalDate.of(1990, 1, 1), 6666, new Mpa(4, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm2);

        // вызываем тестируемый метод
        List<Film> savedFilms = filmStorage.getAll();

        // проверяем утверждения
        assertThat(savedFilms)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newFilm1, newFilm2));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testAddLike() {
        // Подготавливаем данные для теста
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User newUser1 = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        userStorage.add(newUser1);

        User newUser2 = new User(2, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm1 = new Film(1, "Dune", "The most boring movie ever.",
                LocalDate.of(1990, 1, 1), 666, new Mpa(1, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm1);

        Film newFilm2 = new Film(2, "Dune 2", "The most boring movie ever 2.",
                LocalDate.of(1990, 1, 1), 6666, new Mpa(4, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm2);

        filmStorage.addLike(newFilm1.getId(), newUser1.getId());
        filmStorage.addLike(newFilm1.getId(), newUser2.getId());
        filmStorage.addLike(newFilm2.getId(), newUser2.getId());
        // вызываем тестируемый метод
        List<Film> likedFilms = filmStorage.getNFilms(3);

        // проверяем утверждения
        assertThat(likedFilms)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newFilm1, newFilm2));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testRemoveLike() {
        // Подготавливаем данные для теста
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User newUser1 = new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        userStorage.add(newUser1);

        User newUser2 = new User(2, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        User newUser3 = new User(3, "user@email222.ru", "vanya123456", "Ivan0 Petrov",
                LocalDate.of(1991, 2, 3));
        userStorage.add(newUser2);

        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm1 = new Film(1, "Dune", "The most boring movie ever.",
                LocalDate.of(1990, 1, 1), 666, new Mpa(1, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm1);

        Film newFilm2 = new Film(2, "Dune 2", "The most boring movie ever 2.",
                LocalDate.of(1990, 1, 1), 6666, new Mpa(4, null),
                List.of(new Genre(1, null), new Genre(3, null)));
        filmStorage.add(newFilm2);

        filmStorage.addLike(newFilm1.getId(), newUser1.getId());
        filmStorage.addLike(newFilm1.getId(), newUser2.getId());
        filmStorage.addLike(newFilm2.getId(), newUser2.getId());
        filmStorage.addLike(newFilm2.getId(), newUser3.getId());

        filmStorage.removeLike(newFilm1.getId(), newUser1.getId());
        // вызываем тестируемый метод
        List<Film> likedFilms = filmStorage.getNFilms(3);

        // проверяем утверждения
        assertThat(likedFilms)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(List.of(newFilm2, newFilm1));        // и сохраненного пользователя - совпадают
    }
}
