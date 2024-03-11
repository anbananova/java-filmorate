package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Film.
 */
@Data
public class Film {
    private int id;
    @NotNull
    @NotBlank
    private final String name;
    private final String description;
    @NotNull
    private final LocalDate releaseDate;
    private final int duration;
    private Map<Integer, Integer> likes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return id == film.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addLike(Integer userId) {
        try {
            likes.put(userId, 1);
        } catch (NullPointerException e) {
            likes = new HashMap<>();
            likes.put(userId, 1);
        }
    }

    public void removeLike(Integer userId) {
        likes.remove(userId);
    }

    public int getLikeCount() {
        try {
            return likes.values().stream().mapToInt(v -> v).sum();
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
