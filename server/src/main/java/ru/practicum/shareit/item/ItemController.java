package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(final ItemRepository repository, final UserRepository userRepository,
                          final ItemRequestRepository requestRepository, final BookingRepository bookingRepository, final CommentRepository commentRepository) {
        service = new ItemService(repository, userRepository, requestRepository, bookingRepository, commentRepository);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") long id) {
        return service.getItem(id);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getItems(userId);
    }

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.createItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable final long id,
                              @RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemUpdateDto itemUpdateDto) {

        return service.updateItem(id, userId, itemUpdateDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return service.getSearchItems(text);
    }

    @PostMapping("/{itemsId}/comment")
    public CommentDto createComment(@PathVariable long itemsId,
                                    @RequestBody CommentCreateDto commentCreateDto,
                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.createComment(itemsId, commentCreateDto, userId);
    }
}