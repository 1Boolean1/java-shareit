package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemService(ItemRepository repository, UserRepository userRepository,
                       ItemRequestRepository requestRepository, BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public ItemDto getItem(long id) {
        return ItemMapper.mapToItemDto(
                repository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Item not found")));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItems(long userId) {
        if (!isExistsUser(userId)) {
            throw new NotFoundException("User not found");
        }
        return repository.findByOwnerId(userId)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto createItem(ItemDto itemDto, long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = requestRepository.findById(itemDto.getRequestId())
                    .orElse(null);
        }

        Item newItem = ItemMapper.mapToItem(itemDto, owner, request);

        Item savedItem = repository.save(newItem);

        return ItemMapper.mapToItemDto(savedItem);
    }

    public ItemDto updateItem(long itemId, long userId, ItemUpdateDto itemUpdateDto) {
        if (getItem(itemId).getOwnerId() != userId) {
            throw new NotFoundException("Wrong owner id");
        }

        if (itemUpdateDto.getName() == null && itemUpdateDto.getDescription() == null && itemUpdateDto.getAvailable() == null) {
            throw new BadRequestException("No fields to update provided.");
        }

        Item existingItem = repository.findById(itemId)
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
            Item updatedItem = repository.save(existingItem);
            return ItemMapper.mapToItemDto(updatedItem);
        } else {
            return ItemMapper.mapToItemDto(existingItem);
        }
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getSearchItems(String query) {
        if (query.isBlank()) {
            return new ArrayList<>();
        }
        return repository.searchByNameOrDescriptionIgnoreCase(query)
                .stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    public CommentDto createComment(Long itemId, CommentCreateDto commentCreateDto, Long userId) {
        Booking booking = bookingRepository.findByItemIdAndBookerId(itemId, userId);
        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Booking is ending date");
        }

        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setAuthor(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId)));
        comment.setItem(repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId)));
        Comment createComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(createComment);


    }

    private boolean isExistsUser(long id) {
        return userRepository.findAll().stream()
                .anyMatch(userCheck -> userCheck.getId() == id);
    }
}
