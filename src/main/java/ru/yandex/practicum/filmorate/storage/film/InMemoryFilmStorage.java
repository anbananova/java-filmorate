package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final List<Film> films = new ArrayList<>();

    @Override
    public Film add(Film film) {
        film.setId(films.size() + 1);

        if (films.contains(film)) {
            throw new FilmAlreadyExistException("Фильм уже был добавлен: " + film);
        }

        log.debug("Текущий фильм: {}", film);
        films.add(film);
        return film;
    }

    @Override
    public void remove(Film film) {
        if (films.contains(film)) {
            films.remove(film);
        } else {
            throw new NotFoundException("Такого фильма не существует: " + film);
        }
    }

    @Override
    public Film update(Film film) {
        if (films.contains(film)) {
            films.remove(film);
        } else {
            throw new NotFoundException("Такого фильма не существует: " + film);
        }

        log.debug("Текущий фильм: {}", film);
        films.add(film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        log.debug("Текущее количество фильмов: {}", films.size());
        return films;
    }

    @Override
    public Film getFilm(int id) {
        return films.stream().filter(f -> id == f.getId()).findFirst()
                .orElseThrow(() -> new NotFoundException("Такого фильма с id = " + id + " не существует."));
    }
}
