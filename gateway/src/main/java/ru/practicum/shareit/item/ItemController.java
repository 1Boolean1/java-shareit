package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable("id") long id) {
        return itemClient.getItem(id);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return itemClient.getItems(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@PathVariable final long id,
                                             @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId,
                                             @RequestBody ItemUpdateDto itemUpdateDto) {

        return itemClient.updateItem(id, userId, itemUpdateDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        return itemClient.getSearchItems(text);
    }

    @PostMapping("/{itemsId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable long itemsId,
                                                @RequestBody CommentCreateDto commentCreateDto,
                                                @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return itemClient.createComment(itemsId, commentCreateDto, userId);
    }
}