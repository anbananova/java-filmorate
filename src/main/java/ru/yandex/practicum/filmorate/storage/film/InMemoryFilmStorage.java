package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("InMemory")
public class InMemoryFilmStorage implements FilmStorage {
    private final List<Film> films = new ArrayList<>();
    private final Map<List<Integer>, Integer> likesUsers = new HashMap<>();
    private final Map<Integer, Integer> likesFilms = new HashMap<>();

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
    public Optional<Film> getFilm(int id) {
        return Optional.ofNullable(films.stream().filter(f -> id == f.getId()).findFirst()
                .orElseThrow(() -> new NotFoundException("Такого фильма с id = " + id + " не существует.")));
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        likesUsers.put(List.of(userId, filmId), 1);
        likesFilms.put(filmId, likesFilms.getOrDefault(filmId, 0) + 1);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        likesUsers.remove(List.of(userId, filmId));

        // если у фильма уже есть лайки , убираем 1
        if (likesFilms.containsKey(filmId)) {
            int likeCount = likesFilms.get(filmId);
            if (likeCount > 1) {
                likesFilms.put(filmId, likeCount - 1);
            }
        }
    }

    @Override
    public List<Film> getNFilms(Integer count) {
        return likesFilms.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(count)
                .map(e -> getFilm(e.getKey()).orElse(null))
                .collect(Collectors.toList());
    }
}
