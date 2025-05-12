package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.FieldContainsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final ArrayList<User> users;

    public UserRepository(ArrayList<User> users) {
        this.users = users;

    }

    public List<User> getAll() {
        return users;
    }

    public Optional<User> getById(long id) {
        return users.stream().filter(user -> user.getId() == id).findFirst();
    }

    public User insert(User user) {
        user.setId(users.size() + 1);
        checkEmail(user.getEmail());
        users.add(user);
        return user;
    }

    public User update(User user) {
        delete(user.getId());
        checkEmail(user.getEmail());
        users.add(user);
        users.set(users.indexOf(user), user);
        return getById(user.getId()).orElseThrow(() ->
                new RuntimeException("User disappeared after update, id: " + user.getId()));
    }

    public void delete(long id) {
        users.removeIf(user -> user.getId() == id);
    }

    private void checkEmail(String email) {
        if (email.isBlank()) {
            throw new BadRequestException("Email is empty");
        } else if (users.stream().anyMatch(user1 -> user1.getEmail().equals(email))) {
            throw new FieldContainsException("Email is already in use");
        } else if (!email.contains("@") || !email.contains(".")) {
            throw new BadRequestException("Email contains invalid characters");
        }
    }
}
