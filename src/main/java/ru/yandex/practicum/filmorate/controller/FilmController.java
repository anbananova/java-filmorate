package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.FilmAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final List<Film> films = new ArrayList<>();

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Текущее количество фильмов: {}", films.size());
        return films;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilm(film);

        film.setId(films.size() + 1);

        if (films.contains(film)) {
            throw new FilmAlreadyExistException("Фильм уже был добавлен: " + film);
        }

        log.debug("Текущий фильм: {}", film);
        films.add(film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        validateFilm(film);

        if (films.contains(film)) {
            films.remove(film);
        } else {
            throw new ValidationException("Такого фильма не существует: " + film);
        }

        log.debug("Текущий фильм: {}", film);
        films.add(film);
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильмов превышает 200 символов. Длина: "
                    + film.getDescription().length());
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            throw new ValidationException("Дата релиза не может быть в раньше 28 декабря 1895 года: "
                    + film.getReleaseDate());
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной: "
                    + film.getDuration());
        }
    }
}