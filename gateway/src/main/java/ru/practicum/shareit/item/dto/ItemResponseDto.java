package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponseDto {
    @Min(1)
    private Long id;
    @NotBlank
    private String name;
    @Min(1)
    private Long ownerId;
}