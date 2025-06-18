package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Test
    void testSerialize() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemResponseDto itemDto = new ItemResponseDto(1L, "Drill", 10L);
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need a drill",
                2L,
                now,
                List.of(itemDto)
        );

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathNumberValue("$.requesterId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(now.format(formatter));
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(10);
    }

    @Test
    void testDeserialize() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String createdString = now.format(formatter);

        String content = String.format("{\"id\":1,\"description\":\"Need a drill\",\"requesterId\":2,\"created\":\"%s\",\"items\":[{\"id\":1,\"name\":\"Drill\",\"ownerId\":10}]}", createdString);

        ItemRequestDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getRequesterId()).isEqualTo(2L);
        assertThat(result.getCreated()).isEqualTo(now);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(10L);
    }

    @Test
    void testSerialize_noItems() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need something",
                2L,
                now,
                List.of()
        );

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need something");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}