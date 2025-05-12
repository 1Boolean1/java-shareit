package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    @NotNull
    private long id;
    private String description;
    @NotNull
    private long requesterId;
    private LocalDateTime created;
}
