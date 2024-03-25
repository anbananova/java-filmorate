package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("Db")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) {
        jdbcTemplate.update(
                "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration()
        );

        setFilmId(film);
        setFilmRating(film);
        setFilmGenre(film);

        log.debug("Текущий фильм: {}", film);

        return film;
    }

    @Override
    public void remove(Film film) {
        if (getFilm(film.getId()).isPresent()) {
            jdbcTemplate.update(
                    "DELETE FROM films WHERE id = ?", film.getId()
            );
        } else {
            throw new NotFoundException("Такого фильма не существует: " + film);
        }
    }

    @Override
    public Film update(Film film) {
        if (getFilm(film.getId()).isPresent()) {
            jdbcTemplate.update(
                    "UPDATE films SET name = ? WHERE id = ?", film.getName(), film.getId()
            );
            jdbcTemplate.update(
                    "UPDATE films SET description = ? WHERE id = ?", film.getDescription(), film.getId()
            );
            jdbcTemplate.update(
                    "UPDATE films SET release_date = ? WHERE id = ?", film.getReleaseDate(), film.getId()
            );
            jdbcTemplate.update(
                    "UPDATE films SET duration = ? WHERE id = ?", film.getDuration(), film.getId()
            );

            setFilmRating(film);
            setFilmGenre(film);

        } else {
            throw new NotFoundException("Такого фильма не существует: " + film);
        }

        log.debug("Текущий фильм: {}", film);

        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM films";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Optional<Film> getFilm(int id) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);

        if (rows.next()) {
            Film film = new Film(rows.getInt("id"),
                    rows.getString("name"),
                    rows.getString("description"),
                    rows.getDate("release_date").toLocalDate(),
                    rows.getInt("duration"),
                    findMpa(id),
                    findGenres(id)
            );

            log.info("Найден фильм: {}", film);

            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    private void setFilmId(Film film) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE name = ?", film.getName());

        if (rows.next()) {
            int id = rows.getInt("id");
            film.setId(id);

            log.info("Найден id - {} для фильма {}", id, film);

        } else {
            log.info("Фильм с названием {} не найден.", film.getName());
        }
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        return new Film(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                findMpa(rs.getInt("id")),
                findGenres(rs.getInt("id")));
    }

    private void setFilmRating(Film film) {
        try {
            Integer mpaId = film.getMpa().getId();
            Mpa mpaNew;
            if (mpaId != null) {
                SqlRowSet rowsRating = jdbcTemplate.queryForRowSet("SELECT * FROM film_rating WHERE film_id = ?",
                        film.getId());

                if (rowsRating.next()) {
                    jdbcTemplate.update(
                            "DELETE FROM film_rating WHERE film_id = ?",
                            film.getId()
                    );
                }

                SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM ratings WHERE id = ?", mpaId);

                if (rows.next()) {
                    jdbcTemplate.update(
                            "INSERT INTO film_rating (film_id, rating_id) VALUES (?, ?)",
                            film.getId(), mpaId
                    );

                    mpaNew = new Mpa(mpaId, rows.getString("name"));
                    film.setMpa(mpaNew);
                    log.info("Найден name рейтинга - {} для фильма {}", rows.getString("name"), film);
                } else {
                    throw new ValidationException("Не найден name рейтинга для id - " + mpaId);
                }
            } else {
                log.info("Рейтинг фильма {} пустой.", film);
            }
        } catch (NullPointerException e) {
            log.info("Рейтинг фильма {} пустой.", film);
        }
    }

    private Mpa findMpa(int filmId) {
        Mpa mpa = null;
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM film_rating WHERE film_id = ?", filmId);

        if (rows.next()) {
            Integer mpaId = rows.getInt("rating_id");
            log.info("Нашли rating_id рейтинга - {}", mpaId);

            SqlRowSet rowsRating = jdbcTemplate.queryForRowSet("SELECT * FROM ratings WHERE id = ?", mpaId);
            if (rowsRating.next()) {
                String mpaName = rowsRating.getString("name");
                log.info("Нашли name рейтинга - {}", mpaName);

                mpa = new Mpa(mpaId, mpaName);
            } else {
                throw new ValidationException("Не найден name рейтинга для id - " + mpaId);
            }
        }
        return mpa;
    }

    private void setFilmGenre(Film film) {
        List<Genre> genres = film.getGenres();
        List<Genre> genresNew = new ArrayList<>();

        try {
            SqlRowSet rowsGenre = jdbcTemplate.queryForRowSet("SELECT * FROM film_genre WHERE film_id = ?",
                    film.getId());

            if (rowsGenre.next()) {
                jdbcTemplate.update(
                        "DELETE FROM film_genre WHERE film_id = ?",
                        film.getId()
                );
            }

            for (Genre genre : genres) {
                Integer genreId = genre.getId();

                jdbcTemplate.update(
                        "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genreId
                );

                SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", genreId);

                if (rows.next()) {
                    genresNew.add(new Genre(genreId, rows.getString("name")));
                    log.info("Найден name жанра - {} для фильма {}", rows.getString("name"), film);
                } else {
                    throw new ValidationException("Не найден name жанра для id - " + genreId);
                }
            }
        } catch (NullPointerException e) {
            log.info("Жанр фильма {} пустой.", film);
        }

        if (!genresNew.isEmpty()) {
            film.setGenres(genresNew);
        }
    }

    private List<Genre> findGenres(Integer filmId) {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT * FROM film_genre WHERE film_id = ?";

        List<Integer> genreIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("genre_id"), filmId);

        for (Integer genreId : genreIds) {
            SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", genreId);

            if (rows.next()) {
                genres.add(new Genre(genreId, rows.getString("name")));
                log.info("Найден name жанра - {}", rows.getString("name"));
            } else {
                throw new ValidationException("Не найден name жанра для id - " + genreId);
            }
        }

        return genres;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM likes WHERE user_Id = ? AND film_id = ?",
                userId, filmId);

        // ставим лайк фильму только если его не было
        if (!rows.next()) {
            jdbcTemplate.update(
                    "INSERT INTO likes (user_Id, film_id) VALUES (?, ?)", userId, filmId
            );
        }
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM likes WHERE user_Id = ? AND film_id = ?",
                userId, filmId);

        // убираем лайк фильму только если он был
        if (rows.next()) {
            jdbcTemplate.update(
                    "DELETE FROM likes WHERE user_Id = ? AND film_id = ?", userId, filmId
            );
        }
    }

    @Override
    public List<Film> getNFilms(Integer count) {
        String sql = "SELECT film_id, COUNT(*) cnt\n" +
                "FROM likes l\n" +
                "GROUP BY film_id\n" +
                "ORDER BY cnt DESC\n" +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> getFilm(rs.getInt("film_id")).orElse(null), count);
    }
}