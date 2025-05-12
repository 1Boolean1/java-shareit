package ru.practicum.shareit.item;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.base.BaseRepository;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ItemRepository extends BaseRepository<Item> {
    public ItemRepository(JdbcTemplate jdbc, ItemRowMapper mapper) {
        super(jdbc, mapper);
    }

    private final String GET_ALL_ITEMS = "SELECT * FROM items WHERE owner_id = ?";
    private final String GET_ITEM_BY_ID = "SELECT * FROM items WHERE id = ?";
    private final String ADD_ITEM = "INSERT INTO items (name, description, available, owner_id) VALUES (?, ?, ?, ?)";
    private final String UPDATE_QUERY = "UPDATE items SET name = ?, description = ?, available = ? WHERE id = ?";
    private static final String SEARCH_QUERY =
            "SELECT * FROM items WHERE name LIKE CONCAT('%', ?, '%') OR description LIKE CONCAT('%', ?, '%')";

    public Collection<Item> getAllItems(long userId) {
        return findMany(GET_ALL_ITEMS, userId);
    }

    public Optional<Item> getItemById(long id) {
        return findOne(GET_ITEM_BY_ID, id);
    }

    public Item addItem(Item item) {
        long id = insert(ADD_ITEM,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwnerId());
        item.setId(id);
        return item;
    }

    public Item update(Item item) {
        int rowsAffected = jdbc.update(UPDATE_QUERY,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getId());
        if (rowsAffected == 0) {
            throw new NotFoundException("Item with id " + item.getId() + " not found for update.");
        }
        return getItemById(item.getId()).orElseThrow(() ->
                new NotFoundException("User disappeared after update, id: " + item.getId()));
    }

    public Collection<Item> getSearchItems(String query) {
        return findMany(SEARCH_QUERY, query, query);
    }

}
