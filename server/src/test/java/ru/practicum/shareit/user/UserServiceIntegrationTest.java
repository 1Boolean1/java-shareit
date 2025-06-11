package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.FieldContainsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user1, user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();

        user1 = userRepository.save(new User(0L, "User One", "user1@example.com"));
        user2 = userRepository.save(new User(0L, "User Two", "user2@example.com"));
        entityManager.flush();
        entityManager.clear();

        user1 = userRepository.findById(user1.getId()).orElseThrow();
        user2 = userRepository.findById(user2.getId()).orElseThrow();
    }

    @Test
    void createUserWhenValidShouldCreateAndReturnUserDto() {
        UserDto newUserDto = new UserDto(0L, "New User", "new@example.com");
        UserDto created = userService.createUser(newUserDto);

        assertNotNull(created.getId());
        assertThat(created.getName()).isEqualTo("New User");
        assertThat(created.getEmail()).isEqualTo("new@example.com");

        User persistedUser = userRepository.findById(created.getId()).orElse(null);
        assertNotNull(persistedUser);
        assertThat(persistedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void createUserWhenNameIsNullShouldUseEmailAsName() {
        UserDto newUserDto = new UserDto(0L, null, "nameisemail@example.com");
        UserDto created = userService.createUser(newUserDto);

        assertThat(created.getName()).isEqualTo("nameisemail@example.com");
        assertThat(created.getEmail()).isEqualTo("nameisemail@example.com");
    }

    @Test
    void createUserWhenEmailIsNullShouldThrowBadRequestException() {
        UserDto newUserDto = new UserDto(0L, "Test Name", null);
        assertThrows(BadRequestException.class, () -> userService.createUser(newUserDto));
    }

    @Test
    void createUserWhenEmailAlreadyExistsShouldThrowFieldContainsException() {
        UserDto duplicateEmailDto = new UserDto(0L, "Another Name", user1.getEmail());
        assertThrows(FieldContainsException.class, () -> userService.createUser(duplicateEmailDto));
    }

    @Test
    void getUsersShouldReturnAllUsers() {
        Collection<UserDto> users = userService.getUsers();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDto::getEmail).containsExactlyInAnyOrder(user1.getEmail(), user2.getEmail());
    }

    @Test
    void getUserByIdWhenUserExistsShouldReturnUserDto() {
        UserDto found = userService.getUserById(user1.getId());
        assertThat(found.getId()).isEqualTo(user1.getId());
        assertThat(found.getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void getUserByIdWhenUserDoesNotExistShouldThrowNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUserWhenUserExistsAndDataIsValidShouldUpdateAndReturnUserDto() {
        UserUpdateDto updates = new UserUpdateDto("Updated User One", "updated.user1@example.com");
        UserDto updated = userService.updateUser(user1.getId(), updates);

        assertThat(updated.getName()).isEqualTo("Updated User One");
        assertThat(updated.getEmail()).isEqualTo("updated.user1@example.com");

        User persisted = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Updated User One");
        assertThat(persisted.getEmail()).isEqualTo("updated.user1@example.com");
    }

    @Test
    void updateUserOnlyNameShouldUpdateOnlyName() {
        UserUpdateDto updates = new UserUpdateDto("Only Name Updated", null);
        UserDto updated = userService.updateUser(user1.getId(), updates);

        assertThat(updated.getName()).isEqualTo("Only Name Updated");
        assertThat(updated.getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void updateUserOnlyEmailShouldUpdateOnlyEmail() {
        UserUpdateDto updates = new UserUpdateDto(null, "only.email.updated@example.com");
        UserDto updated = userService.updateUser(user1.getId(), updates);

        assertThat(updated.getName()).isEqualTo(user1.getName());
        assertThat(updated.getEmail()).isEqualTo("only.email.updated@example.com");
    }

    @Test
    void updateUserWhenUserDoesNotExistShouldThrowNotFoundException() {
        UserUpdateDto updates = new UserUpdateDto("Name", "email@example.com");
        assertThrows(NotFoundException.class, () -> userService.updateUser(999L, updates));
    }

    @Test
    void updateUserWhenNoFieldsToUpdateShouldThrowBadRequestException() {
        UserUpdateDto emptyUpdates = new UserUpdateDto(null, null);
        assertThrows(BadRequestException.class, () -> userService.updateUser(user1.getId(), emptyUpdates));
    }

    @Test
    void updateUserWhenEmailAlreadyExistsForAnotherUserShouldThrowFieldContainsException() {
        UserUpdateDto updates = new UserUpdateDto(null, user2.getEmail());
        assertThrows(FieldContainsException.class, () -> userService.updateUser(user1.getId(), updates));
    }

    @Test
    void deleteUserWhenIdIsZeroShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> userService.deleteUser(0L));
    }

    @Test
    void deleteUserWhenUserDoesNotExistShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> userService.deleteUser(999L));
    }
}