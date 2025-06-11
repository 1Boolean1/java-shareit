package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerializeCommentDto() throws Exception {
        LocalDateTime created = LocalDateTime.now().minusHours(1).withNano(0); // Consistent time
        ItemShortDto itemShortDto = new ItemShortDto(1L, "Drill", "Powerful drill");

        CommentDto commentDto = new CommentDto(
                5L,
                "Excellent product!",
                itemShortDto,
                "Peter",
                created
        );

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Excellent product!");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Peter");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.format(formatter));
    }

    @Test
    void testDeserializeCommentDto() throws Exception {
        LocalDateTime created = LocalDateTime.now().minusHours(1).withNano(0); // Consistent time
        String createdStr = created.format(formatter);

        String content = String.format(
                "{\"id\":5,\"text\":\"Excellent product!\"," +
                        "\"item\":{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\"}," +
                        "\"authorName\":\"Peter\",\"created\":\"%s\"}",
                createdStr
        );

        CommentDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getText()).isEqualTo("Excellent product!");
        assertThat(result.getItem().getId()).isEqualTo(1L);
        assertThat(result.getItem().getName()).isEqualTo("Drill");
        assertThat(result.getAuthorName()).isEqualTo("Peter");
        assertThat(result.getCreated()).isEqualTo(created);
    }
}