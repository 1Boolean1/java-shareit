package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.util.BookingStatus;

import java.time.LocalDateTime;

@Data
public class Booking {
    @NotNull
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    @NotNull
    private long itemId;
    @NotNull
    private long bookerId;
    @NotNull
    private BookingStatus status;
}
