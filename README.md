# Filmorate
[ER-диаграмма](#er-диаграмма-filmorate-a-nameer-диаграммаa)  
[Описание базы данных](#описание-базы-данных-filmorate)  
[Примеры SQL-запросов](#примеры-sql-запросов-к-базе-данных-filmorate)

### ER-диаграмма Filmorate:
![Filmorate.png](src%2Fmain%2Fresources%2FFilmorate.png)

### Описание базы данных Filmorate:
#### users  
Содержит данные о пользователях.  
Таблица включает такие поля:  
* первичный ключ `id` - идентификатор пользователя;
* `email` - электронная почта пользователя;
* `login` - логин пользователя;
* `birthday` - дата рождения пользователя.
#### friends
Содержит данные о дружеских связях пользователей из таблицы `users`.  
Таблица включает такие поля:
* первичный ключ `user_id` - идентификатор пользователя;
* внешний ключ `friend_id` (отсылает к таблице `users`) - идентификатор друга пользователя;
* внешний ключ `friendship_status_id` (отсылает к таблице `friendship_statuses`) - идентификатор статуса дружбы;
* `last_updated` - дата последнего обновления информации.
#### friendship_statuses
Содержит данные о возможных статусах дружбы.  
Таблица включает такие поля:
* первичный ключ `id` - идентификатор статуса;
* `name` - название статуса дружбы.
#### films
Содержит данные о фильмах.  
Таблица включает такие поля:
* первичный ключ `id` - идентификатор фильма;
* `name` - название фильма;
* `description` - описание фильма;
* `release_date` - дата выхода фильма;
* `duration` - длительность фильма в минутах;
* внешний ключ `rating_id` (отсылает к таблице `ratings`) - идентификатор рейтинга фильма.
#### likes
Содержит данные о лайках фильмам, которые поставили пользователи из таблицы `users`.  
Таблица включает такие поля:
* первичный ключ `user_id` - идентификатор пользователя;
* внешний ключ `film_id` (отсылает к таблице `films`) - идентификатор фильма;
* `last_updated` - дата последнего обновления информации.
#### film_genre
Содержит данные о жанрах фильмов из таблицы `films`.  
Таблица включает такие поля:
* первичный ключ `film_id` - идентификатор фильма;
* внешний ключ `genre_id` (отсылает к таблице `genres`) - идентификатор жанра фильма;
* `last_updated` - дата последнего обновления информации.
#### genres
Содержит данные о жанрах кино.  
Таблица включает такие поля:
* первичный ключ `id` - идентификатор жанра;
* `name` - название жанра.
#### ratings
Содержит данные о рейтингах кино.  
Таблица включает такие поля:
* первичный ключ `id` - идентификатор рейтинга;
* `name` - название рейтинга.
---
### Примеры SQL-запросов к базе данных Filmorate
- Получение всех пользователей:
```sql
SELECT *
FROM users u;
```
- Получение пользователя с идентификатором "3":
```sql
SELECT *
FROM users u
WHERE id = 3;
```
- Получение друзей пользователя с идентификатором "3":
```sql
SELECT *
FROM users u
WHERE id IN 
    (SELECT DISTINCT friend_id
    FROM friends f
    WHERE user_id = 3);
```
- Получение количества друзей пользователя с идентификатором "3" с подтвержденной дружбой:
```sql
SELECT COUNT(DISTINCT f.friend_id)
FROM friends f
INNER JOIN friendship_statuses fs ON f.friendship_status_id = fs.id AND fs.name = 'ACCEPTED'
WHERE f.user_id = 3;
```
- Получение общих друзей пользователей с идентификаторами "3" и "5":
```sql
SELECT *
FROM users u
WHERE id IN 
    (SELECT DISTINCT f1.friend_id
    FROM friends f1
    INNER JOIN friends f2 ON f1.friend_id = f2.friend_id
    WHERE f1.user_id = 3
    AND f2.user_id = 5);
```
- Получение всех фильмов:
```sql
SELECT *
FROM films f;
```
- Получение фильма с идентификатором "1":
```sql
SELECT *
FROM films f
WHERE id = 1;
```
- Получение фильмов, у которых больше одного лайка:
```sql
SELECT *
FROM films f
WHERE id IN
    (SELECT film_id
    FROM likes l
    GROUP BY film_id
    HAVING COUNT(*) > 1);
```
- Получение фильмов, которые понравились пользователю с идентификатором "3":
```sql
SELECT *
FROM films f
WHERE id IN
    (SELECT DISTINCT film_id
    FROM likes l
    WHERE user_id = 3);
```
- Получение 10 наиболее понравившихся названий фильмов жанра "Комедия" с рейтингом "PG":
```sql
SELECT name
FROM
    (SELECT f.*, film_cnt.film_cnt
    FROM films f
    INNER JOIN film_genre fg ON f.id = fg.film_id
    INNER JOIN genres g ON fg.genre_id = g.id AND g.name = 'COMEDY'
    INNER JOIN ratings r ON f.rating_id = r.id AND r.name = 'PG'
    INNER JOIN (SELECT film_id,
                        COUNT(*) cnt
                FROM likes l
                GROUP BY film_id) film_cnt ON f.id = film_cnt.film_id
    ORDER BY film_cnt DESC
    LIMIT 10);
```