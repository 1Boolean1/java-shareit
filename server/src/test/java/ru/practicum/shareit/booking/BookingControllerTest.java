package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingCreateDto bookingCreateDto;
    private UserDto bookerDto;
    private ItemShortDto itemShortDto;
    private final long bookerId = 1L;
    private final long ownerId = 2L;
    private final long bookingId = 1L;
    private final long itemId = 1L;
    private LocalDateTime now;


    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        bookerDto = new UserDto(bookerId, "Booker Name", "booker@example.com");
        itemShortDto = new ItemShortDto(itemId, "Test Item", "Item Description");

        bookingDto = new BookingDto(bookingId, now.plusHours(1), now.plusHours(5), itemShortDto, bookerDto, BookingStatus.WAITING);
        bookingCreateDto = new BookingCreateDto(itemId, now.plusHours(1), now.plusHours(5));
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldReturnBadRequest() throws Exception {
        when(bookingService.createBooking(any(BookingCreateDto.class), eq(bookerId)))
                .thenThrow(new BadRequestException("Item is not available"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_whenDatesInvalid_shouldReturnBadRequest() throws Exception {
        BookingCreateDto invalidDatesDto = new BookingCreateDto(itemId, now.plusHours(5), now.plusHours(1));
        when(bookingService.createBooking(any(BookingCreateDto.class), eq(bookerId)))
                .thenThrow(new BadRequestException("Start date cannot be after end date"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDatesDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void approveOrRejectBooking_whenApproved_shouldReturnApprovedBookingDto() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveOrRejectBooking(eq(bookingId), eq(ownerId), eq(true))).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void approveOrRejectBooking_whenRejected_shouldReturnRejectedBookingDto() throws Exception {
        bookingDto.setStatus(BookingStatus.REJECTED);
        when(bookingService.approveOrRejectBooking(eq(bookingId), eq(ownerId), eq(false))).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(BookingStatus.REJECTED.toString())));
    }

    @Test
    void approveOrRejectBooking_whenUserNotOwner_shouldReturnBadRequest() throws Exception {
        long notOwnerId = 99L;
        when(bookingService.approveOrRejectBooking(eq(bookingId), eq(notOwnerId), eq(true)))
                .thenThrow(new BadRequestException("You are not owner of this booking"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", notOwnerId)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getBooking_whenUserIsBookerOrOwner_shouldReturnBookingDto() throws Exception {
        when(bookingService.getBooking(eq(bookingId), eq(bookerId))).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())));
    }

    @Test
    void getBooking_whenUserNotAuthorized_shouldReturnBadRequest() throws Exception {
        long unauthorizedUserId = 99L;
        when(bookingService.getBooking(eq(bookingId), eq(unauthorizedUserId)))
                .thenThrow(new BadRequestException("Booking is not owned by the user"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", unauthorizedUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_whenBookingNotFound_shouldReturnNotFound() throws Exception {
        when(bookingService.getBooking(eq(999L), eq(bookerId)))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", 999L)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isNotFound());
    }


    @Test
    void getUserBookings_whenDefaultStateAll_shouldReturnListOfBookingDto() throws Exception {
        when(bookingService.getUserBookings(eq(bookerId), eq("ALL"))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())));
    }

    @Test
    void getUserBookings_whenStateIsInvalid_shouldReturnBadRequest() throws Exception {
        when(bookingService.getUserBookings(eq(bookerId), eq("INVALID_STATE")))
                .thenThrow(new BadRequestException("Invalid state"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getOwnerBookings_whenDefaultStateAll_shouldReturnListOfBookingDto() throws Exception {
        when(bookingService.getOwnerBookings(eq(ownerId), eq("ALL"))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())));
    }
}