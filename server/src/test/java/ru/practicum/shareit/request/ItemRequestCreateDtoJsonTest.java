package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a powerful drill");

        JsonContent<ItemRequestCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a powerful drill");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"description\":\"Need a screwdriver set\"}";

        ItemRequestCreateDto result = json.parseObject(content);

        assertThat(result.getDescription()).isEqualTo("Need a screwdriver set");
    }
}