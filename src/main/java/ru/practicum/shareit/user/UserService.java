package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.FieldContainsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.repository = userRepository;
    }

    public UserDto updateUser(long userId, UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getName() == null && userUpdateDto.getEmail() == null) {
            throw new BadRequestException("No fields to update provided.");
        }

        User existingUser = repository.getById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        boolean needsUpdate = false;

        if (userUpdateDto.getName() != null && !userUpdateDto.getName().isBlank()) {
            if (!existingUser.getName().equals(userUpdateDto.getName())) {
                existingUser.setName(userUpdateDto.getName());
                needsUpdate = true;
            }
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank()) {
            final String newEmail = userUpdateDto.getEmail();
            if (!existingUser.getEmail().equals(newEmail)) {
                repository.getByEmail(newEmail).ifPresent(u -> {
                    if (u.getId() != userId) {
                        try {
                            throw new FieldContainsException("Email " + newEmail + " is already in use.");
                        } catch (BadRequestException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                existingUser.setEmail(newEmail);
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            User updatedUser = repository.update(existingUser);
            return UserMapper.mapToUserDto(updatedUser);
        } else {
            return UserMapper.mapToUserDto(existingUser);
        }
    }

    public Collection<UserDto> getUsers() {
        return repository.getAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(@PathVariable long id) {
        return UserMapper.mapToUserDto(repository.getById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found")));
    }

    public UserDto createUser(@RequestBody User user) {
        if (user.getName() == null) {
            user.setName(user.getEmail());
        }
        return UserMapper.mapToUserDto(repository.insert(user));
    }

    public void deleteUser(@PathVariable long id) {
        if (id == 0) {
            throw new BadRequestException("Id can't be 0");
        } else if (!isExistsUser(id)) {
            throw new BadRequestException("User doesn't exists");
        }
        repository.delete(id);
    }

    private boolean isExistsUser(long id) {
        return repository.getAll().stream()
                .anyMatch(userCheck -> userCheck.getId() == id);
    }
}
