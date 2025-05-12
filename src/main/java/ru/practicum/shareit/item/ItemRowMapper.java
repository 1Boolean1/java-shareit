package ru.practicum.shareit.item;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ItemRowMapper implements RowMapper<Item> {
    @Override
    public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setAvailable(rs.getBoolean("available"));
        item.setOwnerId(rs.getLong("owner_id"));
        item.setRequestId(rs.getLong("request_id"));
        return item;
    }
}
