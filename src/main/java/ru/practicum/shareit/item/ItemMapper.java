package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.Comparator;
import java.util.List;

@UtilityClass
public class ItemMapper {
    public static ItemDto mapToItemDto(Item item) {
        Long ownerId = (item.getOwner() != null) ? item.getOwner().getId() : null;
        Long requestId = (item.getRequest() != null) ? item.getRequest().getId() : null;

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setOwnerId(ownerId);
        dto.setRequestId(requestId);
        dto.setComments(item.getComments()
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .toList());

        List<Booking> lastBookings = item.getBookings().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.REJECTED)
                .sorted(Comparator.comparing(Booking::getStart))
                .toList();
        if (!lastBookings.isEmpty()) {
            dto.setLastBooking(BookingMapper.mapToBookingDto(lastBookings.getLast()));
        }

        List<Booking> nextBookings = item.getBookings().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.WAITING)
                .sorted(Comparator.comparing(Booking::getStart))
                .toList();
        if (!nextBookings.isEmpty()) {
            dto.setNextBooking(BookingMapper.mapToBookingDto(nextBookings.getFirst()));
        }

        return dto;
    }

    public static ItemShortDto mapToItemShortDto(Item item) {
        return new ItemShortDto(
                item.getId(),
                item.getName(),
                item.getDescription()
        );
    }

    public static Item mapToItem(ItemDto itemDto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}