package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository) {
        userService = new UserService(userRepository);
    }

    @PostMapping
    private UserDto createUser(@RequestBody @Valid final UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userService.getUsers();
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable final long id,
                              @RequestBody UserUpdateDto userUpdateDto) {
        if (id <= 0) {
            throw new BadRequestException("User ID must be positive.");
        }
        return userService.updateUser(id, userUpdateDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable final long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable final long id) {
        userService.deleteUser(id);
    }
}