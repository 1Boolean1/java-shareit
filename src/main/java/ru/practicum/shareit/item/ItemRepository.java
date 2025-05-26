package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(long userId);

    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Item> searchByNameOrDescriptionIgnoreCase(@Param("searchText") String searchText);
}
