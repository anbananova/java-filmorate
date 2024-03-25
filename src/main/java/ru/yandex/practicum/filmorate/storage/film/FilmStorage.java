package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    void remove(Film film);

    Film update(Film film);

    List<Film> getAll();

    Optional<Film> getFilm(int id);

    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    List<Film> getNFilms(Integer count);
}
