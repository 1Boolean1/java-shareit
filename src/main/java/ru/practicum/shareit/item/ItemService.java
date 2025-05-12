package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Autowired
    public ItemService(ItemRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public ItemDto getItem(long id) {
        return ItemMapper.mapToItemDto(
                repository.getItemById(id)
                        .orElseThrow(() -> new NotFoundException("Item not found")));
    }

    public List<ItemDto> getItems(long userId) {
        if (!isExistsUser(userId)) {
            throw new NotFoundException("User not found");
        }
        return repository.getAllItems(userId)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto createItem(@RequestBody ItemDto itemDto, long userId) {
        if (!isExistsUser(userId)) {
            throw new NotFoundException("User not found");
        }
        itemDto.setOwnerId(userId);
        return ItemMapper.mapToItemDto(repository.addItem(ItemMapper.mapToItem(itemDto)));
    }

    public ItemDto updateItem(long itemId, long userId, ItemUpdateDto itemUpdateDto) {
        if (getItem(itemId).getOwnerId() != userId) {
            throw new NotFoundException("Wrong owner id");
        }

        if (itemUpdateDto.getName() == null && itemUpdateDto.getDescription() == null && itemUpdateDto.getAvailable() == null) {
            throw new BadRequestException("No fields to update provided.");
        }

        Item existingItem = repository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found."));

        boolean needsUpdate = false;

        if (itemUpdateDto.getName() != null && !itemUpdateDto.getName().isBlank()) {
            if (!existingItem.getName().equals(itemUpdateDto.getName())) {
                existingItem.setName(itemUpdateDto.getName());
                needsUpdate = true;
            }
        }

        if (itemUpdateDto.getDescription() != null && !itemUpdateDto.getDescription().isBlank()) {
            final String newDescription = itemUpdateDto.getDescription();
            if (!existingItem.getDescription().equals(newDescription)) {
                existingItem.setDescription(newDescription);
                needsUpdate = true;
            }
        }

        if (itemUpdateDto.getAvailable() != null) {
            final Boolean newAvailble = itemUpdateDto.getAvailable();
            if (!existingItem.getAvailable().equals(newAvailble)) {
                existingItem.setAvailable(newAvailble);
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            Item updatedItem = repository.update(existingItem);
            return ItemMapper.mapToItemDto(updatedItem);
        } else {
            return ItemMapper.mapToItemDto(existingItem);
        }
    }

    public List<ItemDto> getSearchItems(String query) {
        if (query.isBlank()) {
            return new ArrayList<>();
        }
        return repository.getSearchItems(query)
                .stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    private boolean isExistsUser(long id) {
        return userRepository.getAll().stream()
                .anyMatch(userCheck -> userCheck.getId() == id);
    }
}
