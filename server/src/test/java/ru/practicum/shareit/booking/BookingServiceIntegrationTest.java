package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    private User owner, booker, otherUser;
    private Item item1, item2NotAvailable;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        now = LocalDateTime.now();

        owner = userRepository.save(new User(0L, "Item Owner", "owner@example.com"));
        booker = userRepository.save(new User(0L, "Booker User", "booker@example.com"));
        otherUser = userRepository.save(new User(0L, "Other User", "other@example.com"));

        item1 = itemRepository.save(new Item(0L, "Available Item", "Description 1", true, owner, null, List.of(), List.of()));
        item2NotAvailable = itemRepository.save(new Item(0L, "Not Available Item", "Description 2", false, owner, null, List.of(), List.of()));

        entityManager.flush();
        entityManager.clear();

        owner = userRepository.findById(owner.getId()).orElseThrow();
        booker = userRepository.findById(booker.getId()).orElseThrow();
        otherUser = userRepository.findById(otherUser.getId()).orElseThrow();
        item1 = itemRepository.findById(item1.getId()).orElseThrow();
        item2NotAvailable = itemRepository.findById(item2NotAvailable.getId()).orElseThrow();
    }

    @Test
    void createBookingWhenValidShouldCreateAndReturnBookingDto() {
        BookingCreateDto createDto = new BookingCreateDto(item1.getId(), now.plusHours(1), now.plusHours(2));
        BookingDto createdBooking = bookingService.createBooking(createDto, booker.getId());

        assertNotNull(createdBooking.getId());
        assertThat(createdBooking.getItem().getId()).isEqualTo(item1.getId());
        assertThat(createdBooking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(createdBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(createdBooking.getStart()).isEqualTo(now.plusHours(1));

        Booking persisted = bookingRepository.findById(createdBooking.getId()).orElseThrow();
        assertThat(persisted.getItem().getId()).isEqualTo(item1.getId());
        assertThat(persisted.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void createBookingWhenStartOrEndNullShouldThrowBadRequest() {
        BookingCreateDto noStartDto = new BookingCreateDto(item1.getId(), null, now.plusHours(2));
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(noStartDto, booker.getId()));

        BookingCreateDto noEndDto = new BookingCreateDto(item1.getId(), now.plusHours(1), null);
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(noEndDto, booker.getId()));
    }

    @Test
    void createBookingWhenEndBeforeStartShouldThrowBadRequest() {
        BookingCreateDto invalidDatesDto = new BookingCreateDto(item1.getId(), now.plusHours(2), now.plusHours(1));
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(invalidDatesDto, booker.getId()));
    }

    @Test
    void createBookingWhenEndEqualsStartShouldThrowBadRequest() {
        BookingCreateDto equalDatesDto = new BookingCreateDto(item1.getId(), now.plusHours(1), now.plusHours(1));
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(equalDatesDto, booker.getId()));
    }


    @Test
    void createBookingWhenBookerNotFoundShouldThrowNotFound() {
        BookingCreateDto createDto = new BookingCreateDto(item1.getId(), now.plusHours(1), now.plusHours(2));
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(createDto, 999L));
    }

    @Test
    void createBookingWhenItemNotFoundShouldThrowNotFound() {
        BookingCreateDto createDto = new BookingCreateDto(999L, now.plusHours(1), now.plusHours(2));
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(createDto, booker.getId()));
    }

    @Test
    void createBookingWhenItemNotAvailableShouldThrowBadRequest() {
        BookingCreateDto createDto = new BookingCreateDto(item2NotAvailable.getId(), now.plusHours(1), now.plusHours(2));
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(createDto, booker.getId()));
    }

    @Test
    void approveOrRejectBookingWhenApproveShouldSetStatusToApproved() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.WAITING));
        entityManager.flush();
        entityManager.clear();

        BookingDto result = bookingService.approveOrRejectBooking(booking.getId(), owner.getId(), true);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Booking persisted = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveOrRejectBookingWhenRejectShouldSetStatusToRejected() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.WAITING));
        entityManager.flush();
        entityManager.clear();

        BookingDto result = bookingService.approveOrRejectBooking(booking.getId(), owner.getId(), false);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);

        Booking persisted = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveOrRejectBookingWhenBookingNotFoundShouldThrowNotFound() {
        assertThrows(BadRequestException.class, () -> bookingService.approveOrRejectBooking(999L, owner.getId(), true));
    }

    @Test
    void approveOrRejectBookingWhenUserNotOwnerShouldThrowBadRequest() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.WAITING));
        entityManager.flush();
        entityManager.clear();
        assertThrows(BadRequestException.class, () -> bookingService.approveOrRejectBooking(booking.getId(), otherUser.getId(), true));
    }

    @Test
    void getBookingWhenUserIsBookerShouldReturnBookingDto() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.APPROVED));
        entityManager.flush();
        entityManager.clear();

        BookingDto result = bookingService.getBooking(booking.getId(), booker.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingWhenUserIsOwnerShouldReturnBookingDto() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.APPROVED));
        entityManager.flush();
        entityManager.clear();

        BookingDto result = bookingService.getBooking(booking.getId(), owner.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingWhenUserNotAuthorizedShouldThrowBadRequest() {
        Booking booking = bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.APPROVED));
        entityManager.flush();
        entityManager.clear();
        assertThrows(BadRequestException.class, () -> bookingService.getBooking(booking.getId(), otherUser.getId()));
    }

    @Test
    void getUserBookingsByStateShouldFilterAndSortCorrectly() {
        bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, booker, BookingStatus.WAITING));
        bookingRepository.save(new Booking(0L, now.minusHours(2), now.minusHours(1), item1, booker, BookingStatus.CANCELED));
        bookingRepository.save(new Booking(0L, now.minusHours(3), now.minusHours(0), item1, booker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(0L, now.plusHours(3), now.plusHours(4), item1, booker, BookingStatus.REJECTED));
        entityManager.flush();
        entityManager.clear();

        List<BookingDto> futureBookings = bookingService.getUserBookings(booker.getId(), "FUTURE");
        assertThat(futureBookings).hasSize(1);
        assertThat(futureBookings.getFirst().getStatus()).isEqualTo(BookingStatus.WAITING);

        List<BookingDto> pastBookings = bookingService.getUserBookings(booker.getId(), "PAST");
        assertThat(pastBookings).hasSize(1);
        assertThat(pastBookings.getFirst().getStatus()).isEqualTo(BookingStatus.CANCELED);

        List<BookingDto> currentBookings = bookingService.getUserBookings(booker.getId(), "CURRENT");
        assertThat(currentBookings).hasSize(1);
        assertThat(currentBookings.getFirst().getStatus()).isEqualTo(BookingStatus.APPROVED);

        List<BookingDto> rejectedBookings = bookingService.getUserBookings(booker.getId(), "REJECTED");
        assertThat(rejectedBookings).hasSize(1);
        assertThat(rejectedBookings.getFirst().getStatus()).isEqualTo(BookingStatus.REJECTED);

        List<BookingDto> allBookings = bookingService.getUserBookings(booker.getId(), "ALL");
        assertThat(allBookings).hasSize(4);
        assertTrue(allBookings.getFirst().getStart().isBefore(allBookings.get(1).getStart()) || allBookings.get(0).getStart().isEqual(allBookings.get(1).getStart()));
    }

    @Test
    void getUserBookingsWhenUserNotFoundShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(999L, "ALL"));
    }

    @Test
    void getUserBookingsWhenInvalidStateShouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> bookingService.getUserBookings(booker.getId(), "INVALID"));
    }


    @Test
    void getOwnerBookingsByStateShouldFilterAndSortCorrectly() {
        User otherBooker = userRepository.save(new User(0L, "Other Booker", "ob@example.com"));
        entityManager.flush();
        entityManager.clear();
        otherBooker = userRepository.findById(otherBooker.getId()).orElseThrow();


        bookingRepository.save(new Booking(0L, now.plusHours(1), now.plusHours(2), item1, otherBooker, BookingStatus.WAITING));
        bookingRepository.save(new Booking(0L, now.minusHours(2), now.minusHours(1), item1, otherBooker, BookingStatus.CANCELED));
        bookingRepository.save(new Booking(0L, now.minusHours(3), now.minusHours(0), item1, otherBooker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(0L, now.plusHours(3), now.plusHours(4), item1, otherBooker, BookingStatus.REJECTED));
        entityManager.flush();
        entityManager.clear();

        List<BookingDto> futureOwnerBookings = bookingService.getOwnerBookings(owner.getId(), "FUTURE");
        assertThat(futureOwnerBookings).hasSize(1);
        assertThat(futureOwnerBookings.getFirst().getStatus()).isEqualTo(BookingStatus.WAITING);


        List<BookingDto> allOwnerBookings = bookingService.getOwnerBookings(owner.getId(), "ALL");
        assertThat(allOwnerBookings).hasSize(4);
    }

    @Test
    void getOwnerBookingsWhenUserNotFoundShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(999L, "ALL"));
    }
}