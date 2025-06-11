package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.FieldContainsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional
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

        User existingUser = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        boolean needsUpdate = false;

        if (userUpdateDto.getName() != null && !userUpdateDto.getName().isBlank()) {
            if (!existingUser.getName().equals(userUpdateDto.getName())) {
                existingUser.setName(userUpdateDto.getName());
                needsUpdate = true;
            }
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank()) {
            if (repository.findAll().stream().anyMatch(user -> Objects.equals(user.getEmail(), userUpdateDto.getEmail()))) {
                throw new FieldContainsException("Email already exists.");
            }
            final String newEmail = userUpdateDto.getEmail();
            if (!existingUser.getEmail().equals(newEmail)) {
                existingUser.setEmail(newEmail);
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            User updatedUser = repository.save(existingUser);
            return UserMapper.mapToUserDto(updatedUser);
        } else {
            return UserMapper.mapToUserDto(existingUser);
        }
    }

    @Transactional(readOnly = true)
    public Collection<UserDto> getUsers() {
        return repository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(long id) {
        return UserMapper.mapToUserDto(repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found")));
    }

    public UserDto createUser(UserDto userDto) {
        if (userDto.getName() == null) {
            userDto.setName(userDto.getEmail());
        }
        if (userDto.getEmail() == null) {
            throw new BadRequestException("Email is required.");
        }
        if (repository.findAll().stream().anyMatch(user -> Objects.equals(user.getEmail(), userDto.getEmail()))) {
            throw new FieldContainsException("Email already exists.");
        }
        return UserMapper.mapToUserDto(repository.save(UserMapper.mapToUser(userDto)));
    }

    public void deleteUser(long id) {
        if (id == 0) {
            throw new BadRequestException("Id can't be 0");
        } else if (!isExistsUser(id)) {
            throw new BadRequestException("User doesn't exists");
        }
        repository.deleteById(id);
    }

    private boolean isExistsUser(long id) {
        return repository.findAll().stream()
                .anyMatch(userCheck -> userCheck.getId() == id);
    }
}