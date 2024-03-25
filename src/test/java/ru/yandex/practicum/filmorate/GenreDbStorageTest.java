package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindGenreById() {
        // Подготавливаем данные для теста
        GenreStorage genreStorage = new GenreStorage(jdbcTemplate);

        // вызываем тестируемый метод
        Genre savedGenre = genreStorage.getGenreById(1).orElse(null);

        // проверяем утверждения
        assertThat(savedGenre)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(new Genre(1, "Комедия"));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testFindAllGenres() {
        // Подготавливаем данные для теста
        GenreStorage genreStorage = new GenreStorage(jdbcTemplate);

        // вызываем тестируемый метод
        List<Genre> savedGenre = genreStorage.getGenres();

        List<Genre> targetGenre = List.of(new Genre(1, "Комедия"),
                                        new Genre(2, "Драма"),
                                        new Genre(3, "Мультфильм"),
                                        new Genre(4, "Триллер"),
                                        new Genre(5, "Документальный"),
                                        new Genre(6, "Боевик"));

        // проверяем утверждения
        assertThat(savedGenre)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(targetGenre);        // и сохраненного пользователя - совпадают
    }
}
