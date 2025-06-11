package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        ItemShortDto itemShortDto = new ItemShortDto(1L, "Drill", "Powerful drill");
        UserDto bookerDto = new UserDto(2L, "Ivan", "ivan@example.com");

        BookingDto bookingDto = new BookingDto(
                10L,
                start,
                end,
                itemShortDto,
                bookerDto,
                BookingStatus.WAITING
        );

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.format(formatter));
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Ivan");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testDeserializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);

        String content = String.format(
                "{\"id\":10,\"start\":\"%s\",\"end\":\"%s\"," +
                        "\"item\":{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\"}," +
                        "\"booker\":{\"id\":2,\"name\":\"Ivan\",\"email\":\"ivan@example.com\"}," +
                        "\"status\":\"WAITING\"}",
                startStr, endStr
        );

        BookingDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getItem().getId()).isEqualTo(1L);
        assertThat(result.getItem().getName()).isEqualTo("Drill");
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getBooker().getName()).isEqualTo("Ivan");
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}