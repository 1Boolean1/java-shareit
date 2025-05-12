package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService service;

    public ItemController(final ItemRepository repository, final UserRepository userRepository) {
        service = new ItemService(repository, userRepository);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") long id) {
        return service.getItem(id);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return service.getItems(userId);
    }

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid Item item,
                              @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return service.createItem(item, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable final long id,
                              @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId,
                              @RequestBody ItemUpdateDto itemUpdateDto) {

        return service.updateItem(id, userId, itemUpdateDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return service.getSearchItems(text);
    }
}
