package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepositoryMock;

    private User userEntity1;

    @BeforeEach
    void setUp() {
        userEntity1 = new User(1L, "Test User 1", "user1@example.com");

        lenient().when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(userRepositoryMock.findById(userEntity1.getId())).thenReturn(Optional.of(userEntity1));
        lenient().when(userRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        lenient().when(userRepositoryMock.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            if (userToSave.getId() == 0L) {
                userToSave.setId(System.currentTimeMillis() % 1000 + 100);
            }
            return userToSave;
        });
        doNothing().when(userRepositoryMock).deleteById(anyLong());
    }

    @Test
    void createUserWhenEmailIsNullShouldReturnBadRequest() throws Exception {
        UserDto invalidUserDto = new UserDto(0L, "Test Name", null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserWhenEmailAlreadyExistsShouldReturnConflictStatus() throws Exception {
        UserDto existingUserDto = new UserDto(0L, "Existing Name", "user1@example.com");
        when(userRepositoryMock.findAll()).thenReturn(List.of(userEntity1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingUserDto)))
                .andExpect(status().isConflict());
    }


    @Test
    void getUsersShouldReturnListOfUserDtos() throws Exception {
        User userEntity2 = new User(2L, "Test User 2", "user2@example.com");
        when(userRepositoryMock.findAll()).thenReturn(List.of(userEntity1, userEntity2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is(userEntity1.getEmail())))
                .andExpect(jsonPath("$[1].email", is(userEntity2.getEmail())));
    }

    @Test
    void updateUserWhenUserExistsAndDataIsValidShouldReturnUpdatedUserDto() throws Exception {
        long userIdToUpdate = userEntity1.getId();
        UserUpdateDto updateDto = new UserUpdateDto("Updated Name", "updated.email@example.com");

        when(userRepositoryMock.findAll()).thenReturn(List.of(userEntity1));

        User updatedUserEntity = new User(userIdToUpdate, updateDto.getName(), updateDto.getEmail());
        when(userRepositoryMock.save(any(User.class))).thenReturn(updatedUserEntity);

        mockMvc.perform(patch("/users/{id}", userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateDto.getName())))
                .andExpect(jsonPath("$.email", is(updateDto.getEmail())));
    }

    @Test
    void updateUserWhenUserIdIsInvalidShouldReturnNotFoundFromController() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto("Name", "email@example.com");
        mockMvc.perform(patch("/users/{id}", 0L) // Invalid ID by controller's check
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserWhenUserNotFoundShouldReturnNotFound() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto("Name", "email@example.com");
        when(userRepositoryMock.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserWhenEmailAlreadyExistsForAnotherUserShouldReturnConflictStatus() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(null, "existing.other@example.com");
        User otherUserWithEmail = new User(3L, "Other User", "existing.other@example.com");
        when(userRepositoryMock.findAll()).thenReturn(List.of(userEntity1, otherUserWithEmail));

        mockMvc.perform(patch("/users/{id}", userEntity1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUserWhenUserExistsShouldReturnOkStatus() throws Exception {
        when(userRepositoryMock.findAll()).thenReturn(List.of(userEntity1));

        mockMvc.perform(delete("/users/{id}", userEntity1.getId()))
                .andExpect(status().isOk());

        verify(userRepositoryMock, times(1)).deleteById(userEntity1.getId());
    }

    @Test
    void deleteUserWhenIdIsZeroShouldReturnNotFoundFromService() throws Exception {
        mockMvc.perform(delete("/users/{id}", 0L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserWhenUserDoesNotExistShouldReturnNotFoundFromService() throws Exception {
        when(userRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}