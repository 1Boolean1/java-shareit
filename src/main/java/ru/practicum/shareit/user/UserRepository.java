package ru.practicum.shareit.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {
    public UserRepository(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(name, email) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, email = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";

    public List<User> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> getById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Optional<User> getByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public User insert(User user) {
        long id = insert(INSERT_QUERY,
                user.getName(),
                user.getEmail());
        user.setId(id);
        return user;
    }

    public User update(User user) {
        int rowsAffected = jdbc.update(UPDATE_QUERY,
                user.getName(),
                user.getEmail(),
                user.getId());
        if (rowsAffected == 0) {
            throw new RuntimeException("User with id " + user.getId() + " not found for update.");
        }
        return getById(user.getId()).orElseThrow(() ->
                new RuntimeException("User disappeared after update, id: " + user.getId()));
    }

    public void delete(long id) {
        delete(DELETE_QUERY, id);
    }
}
