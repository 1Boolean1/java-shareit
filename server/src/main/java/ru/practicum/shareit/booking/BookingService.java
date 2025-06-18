package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

import java.util.Comparator;
import java.util.List;

@Transactional
@Slf4j
@Service
public class BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingService(BookingRepository repository, UserRepository userRepository, ItemRepository itemRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public BookingDto createBooking(BookingCreateDto booking, Long bookerId) {

        if (userRepository.findById(bookerId).isEmpty()) {
            log.warn("User not found");
            throw new NotFoundException("User not found");
        }

        if (!itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found")).getAvailable()) {
            throw new BadRequestException("Item is not available");
        }

        Booking newBooking = new Booking();
        newBooking.setStatus(BookingStatus.WAITING);
        newBooking.setStart(booking.getStart());
        newBooking.setEnd(booking.getEnd());

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));
        newBooking.setItem(item);
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        newBooking.setBooker(booker);

        Booking savedBooking = repository.save(newBooking);

        return BookingMapper.mapToBookingDto(savedBooking);
    }

    public BookingDto approveOrRejectBooking(long bookingId, long userId, boolean approved) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (booking.getItem().getOwner().getId() != userId) {
            throw new BadRequestException("You are not owner of this booking");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
            return BookingMapper.mapToBookingDto(booking);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            return BookingMapper.mapToBookingDto(booking);
        }
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(Long bookingId, Long userId) throws BadRequestException {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (booking.getBooker().getId() == userId
                || booking.getItem().getOwner().getId() == userId) {
            return BookingMapper.mapToBookingDto(booking);
        } else {
            throw new BadRequestException("Booking is not owned by the user");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId, String state) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found");
        }
        List<Booking> bookings = repository.findByBookerId(userId);

        return getBookingDtos(state, bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found");
        }

        List<Booking> bookings = repository.findByOwnerId(userId);

        return getBookingDtos(state, bookings);
    }

    private List<BookingDto> getBookingDtos(String state, List<Booking> bookings) {
        return switch (state) {
            case "CURRENT" -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::mapToBookingDto).toList();
            case "PAST" -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.CANCELED))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::mapToBookingDto).toList();
            case "FUTURE" -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::mapToBookingDto).toList();
            case "REJECTED" -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::mapToBookingDto).toList();
            case "ALL" -> bookings.stream()
                    .sorted(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::mapToBookingDto).toList();
            default -> throw new IllegalStateException("Unexpected value: " + state);
        };
    }
}