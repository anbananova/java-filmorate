package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("Db") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
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

    public Film create(@Valid Film film) {
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film update(@Valid Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film getFilmById(Integer filmId) {
        return filmStorage.getFilm(filmId).orElse(null);
    }

    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId).orElse(null);
        if (film != null) {
            filmStorage.addLike(filmId, userId);
        }

        log.debug("addLike Текущий фильм: {}", film);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId).orElse(null);
        if (film != null) {
            filmStorage.removeLike(filmId, userId);
        }

        log.debug("removeLike Текущий фильм: {}", film);
        return film;
    }

    public List<Film> getNFilms(Integer count) {
        log.debug("getNFilms Вернуть {} фильмов.", count);
        return filmStorage.getNFilms(count);
    }
}
