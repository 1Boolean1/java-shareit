package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public BookingDto createBooking(@RequestBody @Valid BookingCreateDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") @Min(1) Long bookerId) {
        return service.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveOrRejectBooking(@PathVariable Long bookingId,
                                             @RequestHeader("X-Sharer-User-Id") @Min(1) Long ownerId,
                                             @RequestParam Boolean approved) {
        return service.approveOrRejectBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader("X-Sharer-User-Id") @Min(1) Long userId) {
        return service.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") @Min(1) Long userId,
                                            @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return service.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") @Min(1) Long userId,
                                             @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return service.getOwnerBookings(userId, state);
    }
}
