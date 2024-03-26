package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("id"),
                rs.getString("name"));
    }

    public Optional<Genre> getGenreById(Integer genreId) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", genreId);

        if (rows.next()) {
            Genre genre = new Genre(rows.getInt("id"),
                    rows.getString("name"));

            log.info("Найден жанр: {}", genre);

            return Optional.of(genre);
        } else {
            throw new NotFoundException("Жанр с идентификатором не найден: " + genreId);
        }
    }
}
