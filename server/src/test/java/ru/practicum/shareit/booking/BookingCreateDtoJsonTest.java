package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Test
    void testSerializeBookingCreateDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0); // Remove nanos for cleaner string comparison
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);
        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.format(formatter));
    }

    @Test
    void testDeserializeBookingCreateDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);

        String content = String.format("{\"itemId\":1,\"start\":\"%s\",\"end\":\"%s\"}", startStr, endStr);

        BookingCreateDto result = json.parseObject(content);

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
    }
}