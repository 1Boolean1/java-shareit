package ru.practicum.shareit.booking;

import java.util.Arrays;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exceptions.BadRequestException;

@Service
@Slf4j
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        if (!Arrays.stream(BookingState.values()).toList().contains(state)) {
            throw new BadRequestException("Booking state must be one of " + Arrays.toString(BookingState.values()));
        }
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }


    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto booking) {
        if (booking.getStart() == null || booking.getEnd() == null) {
            log.warn("Start and end should be set");
            throw new BadRequestException("Start and end should be set");
        }

        if (booking.getStart().isAfter(booking.getEnd()) || booking.getEnd().equals(booking.getStart())) {
            log.warn("Start should be after end");
            throw new BadRequestException("Start date cannot be after end date");
        }
        return post("", userId, booking);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approveOrRejectBooking(long bookingId, long ownerId, boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, ownerId);
    }

    public ResponseEntity<Object> getOwnerBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        if (!Arrays.stream(BookingState.values()).toList().contains(state)) {
            throw new BadRequestException("Booking state must be one of " + Arrays.toString(BookingState.values()));
        }
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}
