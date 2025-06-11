package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.practicum.shareit.user.User;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRepository itemRepositoryMock;
    @MockBean
    private UserRepository userRepositoryMock;
    @MockBean
    private ItemRequestRepository itemRequestRepositoryMock;
    @MockBean
    private BookingRepository bookingRepositoryMock;
    @MockBean
    private CommentRepository commentRepositoryMock;


    private ItemDto itemDto;
    private CommentDto commentDtoResult;
    private final long userId = 1L;
    private final long itemId = 1L;
    private User userStub;
    private Item itemStub;

    @BeforeEach
    void setUp() {
        ItemShortDto itemShortDto = new ItemShortDto(itemId, "Test Item", "Description");
        UserDto bookerUserDto = new UserDto(2L, "Booker User", "booker@example.com");

        BookingDto lastBooking = new BookingDto(10L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                itemShortDto, bookerUserDto, BookingStatus.REJECTED);
        BookingDto nextBooking = new BookingDto(11L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                itemShortDto, bookerUserDto, BookingStatus.WAITING);

        CommentDto itemComment = new CommentDto(5L, "Comment for item", itemShortDto, "Comment Author", LocalDateTime.now().minusHours(1));

        itemDto = new ItemDto(itemId, "Test Item", "Description", true, userId, null,
                List.of(itemComment), lastBooking, nextBooking);

        commentDtoResult = new CommentDto(1L, "Excellent comment", itemShortDto, "Author", LocalDateTime.now());

        userStub = new User(userId, "Test User", "test@example.com");
        itemStub = new Item(itemId, "Test Item", "Description", true, userStub, null, Collections.emptyList(), Collections.emptyList());
        lenient().when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(userStub));
        lenient().when(userRepositoryMock.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(userId)) return Optional.of(userStub);
            if (id.equals(bookerUserDto.getId()))
                return Optional.of(new User(bookerUserDto.getId(), bookerUserDto.getName(), bookerUserDto.getEmail()));
            return Optional.empty();
        });
        lenient().when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(itemStub));
        lenient().when(itemRepositoryMock.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(itemId)) return Optional.of(itemStub);
            return Optional.empty();
        });
        lenient().when(userRepositoryMock.findAll()).thenReturn(List.of(userStub));
        lenient().when(itemRepositoryMock.searchByNameOrDescriptionIgnoreCase(anyString())).thenReturn(Collections.emptyList());
        lenient().when(bookingRepositoryMock.findByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(null);
        lenient().when(commentRepositoryMock.save(any(ru.practicum.shareit.comment.Comment.class))).thenAnswer(invocation -> {
            ru.practicum.shareit.comment.Comment c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreationDate(LocalDateTime.now());
            return c;
        });
        lenient().when(itemRepositoryMock.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }

    @Test
    void getItemWhenItemFoundShouldReturnItemDtoWithDetails() throws Exception {
        Item itemWithDetails = new Item(itemId, "Test Item", "Description", true, userStub, null,
                List.of(new ru.practicum.shareit.comment.Comment(5L, "Comment for item", itemStub, new User(2L, "Comment Author", "ca@ex.com"), LocalDateTime.now().minusHours(1))),
                List.of(
                        new ru.practicum.shareit.booking.Booking(10L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), itemStub, new User(2L, "Booker User", "booker@example.com"), BookingStatus.REJECTED),
                        new ru.practicum.shareit.booking.Booking(11L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), itemStub, new User(2L, "Booker User", "booker@example.com"), BookingStatus.WAITING)
                )
        );
        when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(itemWithDetails));


        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.lastBooking.id", is(10)))
                .andExpect(jsonPath("$.comments[0].text", is("Comment for item")));
    }

    @Test
    void createItemWhenDataIsValidShouldReturnCreatedItemDto() throws Exception {
        ItemDto requestDto = new ItemDto(null, "New Test Item", "New Description", true, userId, null, null, null, null);

        User owner = new User(userId, "Test User", "test@example.com");
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(owner));

        Item savedItem = new Item();
        savedItem.setId(2L);
        savedItem.setName(requestDto.getName());
        savedItem.setDescription(requestDto.getDescription());
        savedItem.setAvailable(requestDto.getAvailable());
        savedItem.setOwner(owner);
        savedItem.setComments(Collections.emptyList());
        savedItem.setBookings(Collections.emptyList());

        when(itemRepositoryMock.save(any(Item.class))).thenAnswer(invocation -> {
            Item itemToSave = invocation.getArgument(0);
            if (itemToSave.getId() == 0L) {
                itemToSave.setId(2L);
            }
            if (itemToSave.getComments() == null) itemToSave.setComments(Collections.emptyList());
            if (itemToSave.getBookings() == null) itemToSave.setBookings(Collections.emptyList());
            return itemToSave;
        });


        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2))) // Ожидаемый ID
                .andExpect(jsonPath("$.name", is(requestDto.getName())));
    }

    @Test
    void createCommentWhenDataIsValidShouldReturnCreatedCommentDto() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("Comment text");

        ru.practicum.shareit.booking.Booking bookingStub = new ru.practicum.shareit.booking.Booking(
                1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                itemStub, userStub, BookingStatus.APPROVED
        );
        when(bookingRepositoryMock.findByItemIdAndBookerId(itemId, userId)).thenReturn(bookingStub);

        ru.practicum.shareit.comment.Comment savedComment = new ru.practicum.shareit.comment.Comment();
        savedComment.setId(commentDtoResult.getId());
        savedComment.setText(commentCreateDto.getText());
        savedComment.setItem(itemStub);
        savedComment.setAuthor(userStub);
        savedComment.setCreationDate(commentDtoResult.getCreated());

        when(commentRepositoryMock.save(any(ru.practicum.shareit.comment.Comment.class))).thenReturn(savedComment);


        mockMvc.perform(post("/items/{itemsId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoResult.getId().intValue())))
                .andExpect(jsonPath("$.text", is(commentCreateDto.getText())));
    }

    @Test
    void getItemWhenItemNotFoundShouldReturnNotFound() throws Exception {
        when(itemRepositoryMock.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemWhenDataIsValidShouldReturnUpdatedItemDto() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated name", null, null);
        Item itemToUpdate = new Item(itemId, "Old Name", "Old Description", true, userStub, null, Collections.emptyList(), Collections.emptyList());
        when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(itemToUpdate));
        Item updatedItemEntity = new Item(itemId, "Updated name", "Old Description", true, userStub, null, Collections.emptyList(), Collections.emptyList());
        when(itemRepositoryMock.save(any(Item.class))).thenReturn(updatedItemEntity);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated name")));
    }

    @Test
    void updateItemWhenNotOwnerShouldReturnNotFound() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Name", null, null);
        long wrongUserId = 2L;
        when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(itemStub));


        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", wrongUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void searchItemsWhenTextIsProvidedShouldReturnListOfFoundItemDto() throws Exception {
        String searchText = "item";
        Item foundItemEntity = new Item(itemId, "Found item", "Description", true, userStub, null, Collections.emptyList(), Collections.emptyList());
        when(itemRepositoryMock.searchByNameOrDescriptionIgnoreCase(searchText)).thenReturn(List.of(foundItemEntity));


        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(foundItemEntity.getName())));
    }

    @Test
    void searchItemsWhenTextIsEmptyShouldReturnEmptyList() throws Exception {

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    void createCommentWhenDataIsInvalidShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("Text");
        when(bookingRepositoryMock.findByItemIdAndBookerId(itemId, userId)).thenReturn(null);

        mockMvc.perform(post("/items/{itemsId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isNotFound());
    }
}