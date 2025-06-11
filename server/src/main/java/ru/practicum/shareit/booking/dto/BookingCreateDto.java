package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {
    @Min(1)
    @NotNull
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}