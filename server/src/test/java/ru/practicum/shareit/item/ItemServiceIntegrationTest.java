package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@SpringBootTest
@Transactional
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item1, item2;
    private ItemRequest itemRequest;
    private Comment commentOnItem1;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        owner = userRepository.save(new User(1L, "Owner Name", "owner@example.com"));
        booker = userRepository.save(new User(2L, "Booker Name", "booker@example.com"));
        User requester = userRepository.save(new User(3L, "Requester Name", "requester@example.com"));

        itemRequest = itemRequestRepository.save(new ItemRequest(0L, "Need a drill", requester, LocalDateTime.now().minusDays(10), Collections.emptyList()));

        item1 = new Item(0L, "Drill", "Powerful drill", true, owner, itemRequest,
                new java.util.ArrayList<>(), new java.util.ArrayList<>());
        item1 = itemRepository.save(item1);

        item2 = new Item(0L, "Hammer", "Heavy hammer", false, owner, null,
                new java.util.ArrayList<>(), new java.util.ArrayList<>());
        item2 = itemRepository.save(item2);

        commentOnItem1 = new Comment(null, "Excellent drill, highly recommend!", item1, booker, LocalDateTime.now().minusDays(5));
        commentOnItem1 = commentRepository.save(commentOnItem1);

        bookingRepository.save(new Booking(0L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2), item1, booker, BookingStatus.REJECTED));
        bookingRepository.save(new Booking(0L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, booker, BookingStatus.WAITING));
        bookingRepository.save(new Booking(0L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(0), item1, booker, BookingStatus.APPROVED));

        entityManager.flush();
        entityManager.clear();

        owner = userRepository.findById(owner.getId()).orElseThrow();
        booker = userRepository.findById(booker.getId()).orElseThrow();
        item1 = itemRepository.findById(item1.getId()).orElseThrow();
        item2 = itemRepository.findById(item2.getId()).orElseThrow();
    }

    @Test
    void getItemWhenItemExistsShouldReturnItemDtoWithCommentsAndBookings() {
        ItemDto result = itemService.getItem(item1.getId());

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getOwnerId()).isEqualTo(owner.getId());
        assertThat(result.getRequestId()).isEqualTo(itemRequest.getId());

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getText()).isEqualTo(commentOnItem1.getText());
        assertThat(result.getComments().getFirst().getAuthorName()).isEqualTo(booker.getName());
        assertThat(result.getComments().getFirst().getItem().getId()).isEqualTo(item1.getId());

        assertNotNull(result.getLastBooking());
        assertThat(result.getLastBooking().getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(result.getLastBooking().getItem().getId()).isEqualTo(item1.getId());

        assertNotNull(result.getNextBooking());
        assertThat(result.getNextBooking().getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getNextBooking().getItem().getId()).isEqualTo(item1.getId());
    }

    @Test
    void getItemWhenItemHasNoCommentsOrBookingsShouldReturnItemDtoWithoutThem() {
        ItemDto result = itemService.getItem(item2.getId());

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("Hammer");
        assertThat(result.getComments()).isEmpty();
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }


    @Test
    void getItemWhenItemDoesNotExistShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getItem(999L));
    }

    @Test
    void getItemsWhenUserExistsShouldReturnUserItemsList() {
        List<ItemDto> result = itemService.getItems(owner.getId());
        assertThat(result).hasSize(2);

        ItemDto resultItem1 = result.stream().filter(dto -> dto.getId().equals(item1.getId())).findFirst().orElseThrow();
        assertThat(resultItem1.getComments()).hasSize(1);
        assertNotNull(resultItem1.getLastBooking());
        assertNotNull(resultItem1.getNextBooking());

        ItemDto resultItem2 = result.stream().filter(dto -> dto.getId().equals(item2.getId())).findFirst().orElseThrow();
        assertThat(resultItem2.getComments()).isEmpty();
        assertNull(resultItem2.getLastBooking());
        assertNull(resultItem2.getNextBooking());
    }


    @Test
    void getItemsWhenUserDoesNotExistShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getItems(999L));
    }

    @Test
    void createItemWhenDataIsValidShouldCreateAndReturnItemDto() {
        ItemDto newItemDto = new ItemDto(null, "New Saw", "Sharp", true, owner.getId(), null, null, null, null);
        ItemDto created = itemService.createItem(newItemDto, owner.getId());

        assertNotNull(created.getId());
        assertThat(created.getName()).isEqualTo("New Saw");
        assertThat(created.getOwnerId()).isEqualTo(owner.getId());
        assertThat(created.getComments()).isNotNull().isEmpty();
        assertNull(created.getLastBooking());
        assertNull(created.getNextBooking());

        Item persistedItem = itemRepository.findById(created.getId()).orElse(null);
        assertNotNull(persistedItem);
        assertThat(persistedItem.getName()).isEqualTo("New Saw");
    }

    @Test
    void createItemWithRequestIdShouldSetRequestRelation() {
        ItemDto newItemDto = new ItemDto(null, "Response to request", "For request", true, owner.getId(), itemRequest.getId(), null, null, null);
        ItemDto created = itemService.createItem(newItemDto, owner.getId());

        assertThat(created.getRequestId()).isEqualTo(itemRequest.getId());
        Item persistedItem = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(persistedItem.getRequest()).isNotNull();
        assertThat(persistedItem.getRequest().getId()).isEqualTo(itemRequest.getId());
    }

    @Test
    void createItemWhenUserDoesNotExistShouldThrowNotFoundException() {
        ItemDto newItemDto = new ItemDto(null, "Some Item", "Description", true, 999L, null, null, null, null);
        assertThrows(NotFoundException.class, () -> itemService.createItem(newItemDto, 999L));
    }

    @Test
    void updateItemWhenDataIsValidAndUserIsOwnerShouldUpdateAndReturnItemDto() {
        ItemUpdateDto updates = new ItemUpdateDto("Updated Drill", null, false);
        ItemDto updated = itemService.updateItem(item1.getId(), owner.getId(), updates);

        assertThat(updated.getName()).isEqualTo("Updated Drill");
        assertThat(updated.getAvailable()).isFalse();
        assertThat(updated.getDescription()).isEqualTo(item1.getDescription());
        assertThat(updated.getComments()).hasSize(1);
        assertNotNull(updated.getLastBooking());
        assertNotNull(updated.getNextBooking());

        Item persisted = itemRepository.findById(item1.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Updated Drill");
        assertThat(persisted.getAvailable()).isFalse();
    }

    @Test
    void updateItemWhenUserIsNotOwnerShouldThrowNotFoundException() {
        ItemUpdateDto updates = new ItemUpdateDto("Attempt", null, null);
        assertThrows(NotFoundException.class, () -> itemService.updateItem(item1.getId(), booker.getId(), updates));
    }

    @Test
    void searchItemsWhenMatchesExistAndItemsAvailableShouldReturnListWithDetails() {
        item2.setAvailable(true);
        itemRepository.saveAndFlush(item2);
        entityManager.clear();

        List<ItemDto> results = itemService.getSearchItems("drill");
        assertThat(results).hasSize(1);
        ItemDto foundItem1 = results.getFirst();
        assertThat(foundItem1.getName()).isEqualTo("Drill");
        assertThat(foundItem1.getComments()).hasSize(1);
        assertNotNull(foundItem1.getLastBooking());

        results = itemService.getSearchItems("hammer");
        assertThat(results).hasSize(1);
        ItemDto foundItem2 = results.getFirst();
        assertThat(foundItem2.getName()).isEqualTo("Hammer");
        assertThat(foundItem2.getComments()).isEmpty();
    }

    @Test
    void searchItemsWhenItemNotAvailableShouldNotReturnIt() {
        List<ItemDto> results = itemService.getSearchItems("hammer");
        assertThat(results).isEmpty();
    }

    @Test
    void searchItemsWhenQueryIsEmptyShouldReturnEmptyList() {
        List<ItemDto> results = itemService.getSearchItems("");
        assertThat(results).isEmpty();
        results = itemService.getSearchItems("   ");
        assertThat(results).isEmpty();
    }

    @Test
    void createCommentWhenNoBookingForUserAndItemShouldThrowNotFoundException() {
        CommentCreateDto commentCreateDto = new CommentCreateDto("Some comment...");
        assertThrows(NotFoundException.class, () -> itemService.createComment(item2.getId(), commentCreateDto, booker.getId()));
    }
}