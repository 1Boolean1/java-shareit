package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class ItemRequestService {
    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestService(ItemRequestRepository repository, UserRepository userRepository, ItemRepository itemRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public ItemRequestDto create(ItemRequestCreateDto dto, long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest newRequest = new ItemRequest();
        newRequest.setDescription(dto.getDescription());
        newRequest.setCreated(LocalDateTime.now());
        newRequest.setRequester(requester);
        ItemRequest savedRequest = repository.save(newRequest);
        return ItemRequestMapper.mapToItemRequestDto(savedRequest, null);
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getRequests(long userId) {
        if (!isExistsUser(userId)) {
            throw new NotFoundException("User not found");
        }
        return repository.findByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(request -> ItemRequestMapper.mapToItemRequestDto(request, itemRepository.findByRequestId(request.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getRequestsFromOtherUsers(long userId) {
        if (!isExistsUser(userId)) {
            throw new NotFoundException("User not found");
        }
        return repository.findByRequesterIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(request -> ItemRequestMapper.mapToItemRequestDto(request, itemRepository.findByRequestId(request.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemRequestDto getRequest(long requestId) {
        return ItemRequestMapper.mapToItemRequestDto(
                repository.findById(requestId)
                        .orElseThrow(() -> new NotFoundException("Request not found")), itemRepository.findByRequestId(requestId)
        );
    }

    private boolean isExistsUser(long id) {
        return userRepository.findAll().stream()
                .anyMatch(userCheck -> userCheck.getId() == id);
    }
}
