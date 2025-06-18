package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;


@Controller
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserClient client;

    @Autowired
    public UserController(UserClient client) {
        this.client = client;
        log.info("<<<<< UserController CONSTRUCTOR CALLED. UserClient is null: {} >>>>>", (this.client == null));
    }

    @PostMapping
    private ResponseEntity<Object> createUser(@RequestBody @Valid final UserDto userDto) {
        return client.createUser(userDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return client.getUsers();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable final long id,
                                             @RequestBody UserUpdateDto userUpdateDto) {
        if (id <= 0) {
            throw new BadRequestException("User ID must be positive.");
        }
        return client.updateUser(id, userUpdateDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable final long id) {
        return client.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable final long id) {
        return client.deleteUser(id);
    }
}