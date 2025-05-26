package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long id);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByOwnerId(Long ownerId);

    Booking findByItemIdAndBookerId(Long itemId, Long bookerId);
}
