package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Service
@Slf4j
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
        log.info("<<<<< UserClient CONSTRUCTOR CALLED with serverUrl: {} >>>>>", serverUrl);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new BadRequestException("Email is required.");
        }
        return post("", userDto);
    }

    public ResponseEntity<Object> updateUser(long id, UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getName() == null && userUpdateDto.getEmail() == null) {
            throw new BadRequestException("No fields to update provided.");
        }
        return patch("/" + id, userUpdateDto);
    }

    public ResponseEntity<Object> getUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUserById(long id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> deleteUser(long id) {
        if (id == 0) {
            throw new BadRequestException("Id can't be 0");
        }
        return delete("/" + id);
    }

}
