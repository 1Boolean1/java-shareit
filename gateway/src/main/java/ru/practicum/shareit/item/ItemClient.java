package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getItem(long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long itemId, long userId, ItemUpdateDto itemUpdateDto) {
        return patch("/" + itemId, userId, itemUpdateDto);
    }

    public ResponseEntity<Object> getSearchItems(String query) {
        return get("/search?text=" + query);
    }

    public ResponseEntity<Object> createComment(long itemId, CommentCreateDto commentCreateDto, long userId) {
        return post("/" + itemId + "/comment", userId, commentCreateDto);
    }
}
