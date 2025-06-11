package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService service;

    public ItemRequestController(final ItemRequestRepository repository, final UserRepository userRepository, final ItemRepository itemRepository) {
        service = new ItemRequestService(repository, userRepository, itemRepository);
    }

    @GetMapping
    public List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getRequestsFromOtherUsers(userId);
    }

    @GetMapping("/{id}")
    public ItemRequestDto getRequest(@PathVariable("id") Long id) {
        return service.getRequest(id);
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemRequestCreateDto dto) {
        return service.create(dto, userId);
    }
}