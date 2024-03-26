package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindMpaById() {
        // Подготавливаем данные для теста
        MpaStorage mpaStorage = new MpaStorage(jdbcTemplate);

        // вызываем тестируемый метод
        Mpa savedMpa = mpaStorage.getMpaById(1).orElse(null);

        // проверяем утверждения
        assertThat(savedMpa)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(new Mpa(1, "G"));        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testFindAllMpas() {
        // Подготавливаем данные для теста
        MpaStorage mpaStorage = new MpaStorage(jdbcTemplate);

        // вызываем тестируемый метод
        List<Mpa> savedMpa = mpaStorage.getMpa();

        List<Mpa> targetMpa = List.of(new Mpa(1, "G"),
                new Mpa(2, "PG"),
                new Mpa(3, "PG-13"),
                new Mpa(4, "R"),
                new Mpa(5, "NC-17"));

        // проверяем утверждения
        assertThat(savedMpa)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(targetMpa);        // и сохраненного пользователя - совпадают
    }
}
