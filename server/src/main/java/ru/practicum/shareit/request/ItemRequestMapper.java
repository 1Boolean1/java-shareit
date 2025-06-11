package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto mapToItemRequestDto(ItemRequest request, List<Item> itemsFromRequest) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        if (request.getRequester() != null) {
            dto.setRequesterId(request.getRequester().getId());
        }
        dto.setCreated(request.getCreated());

        if (itemsFromRequest != null && !itemsFromRequest.isEmpty()) {
            dto.setItems(itemsFromRequest.stream()
                    .map(ItemRequestMapper::mapToItemResponseDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    public static ItemResponseDto mapToItemResponseDto(Item item) {
        if (item == null) return null;
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getOwner() != null ? item.getOwner().getId() : null
        );
    }
}