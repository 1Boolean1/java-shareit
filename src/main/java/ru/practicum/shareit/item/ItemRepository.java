package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Repository
public class ItemRepository {
    private final ArrayList<Item> items;

    public ItemRepository(ArrayList<Item> items) {
        this.items = items;
    }

    public Collection<Item> getAllItems(long userId) {
        return items.stream().filter(item -> item.getOwnerId() == userId)
                .toList();
    }

    public Optional<Item> getItemById(long id) {
        return items.stream().filter(item -> item.getId() == id).findFirst();
    }

    public Item addItem(Item item) {
        if (item.getDescription().isBlank() || item.getName().isBlank() || item.getAvailable() == null) {
            throw new BadRequestException("Item must have description, name, available");
        }
        item.setId(items.size() + 1);
        items.add(item);
        return item;
    }

    public Item update(Item item) {
        items.set(items.indexOf(item), item);
        return getItemById(item.getId()).orElseThrow(() ->
                new NotFoundException("User disappeared after update, id: " + item.getId()));
    }

    public Collection<Item> getSearchItems(String query) {
        return items.stream().filter(item ->
                        item.getName().toLowerCase().contains(query.toLowerCase())
                                || item.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

}
