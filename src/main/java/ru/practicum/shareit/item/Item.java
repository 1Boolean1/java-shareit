package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @NotNull
    private long id;
    @NotBlank(message = "Name can't be null")
    private String name;
    @NotBlank(message = "Description can't be null")
    private String description;
    @NotNull(message = "status can't be null")
    private Boolean available;
    @NotNull
    private long ownerId;
    private long requestId;
}
