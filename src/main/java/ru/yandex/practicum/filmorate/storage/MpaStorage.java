package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getMpa() {
        String sql = "SELECT * FROM ratings";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeMpa(rs));
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("id"),
                rs.getString("name"));
    }

    public Optional<Mpa> getMpaById(Integer id) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM ratings WHERE id = ?", id);

        if (rows.next()) {
            Mpa mpa = new Mpa(rows.getInt("id"),
                    rows.getString("name"));

            log.info("Найден рейтинг: {}", mpa);

            return Optional.of(mpa);
        } else {
            throw new NotFoundException("Рейтин с идентификатором не найден: " + id);
        }
    }
}

