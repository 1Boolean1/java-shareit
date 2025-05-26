package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.ItemMapper;

public class CommentMapper {
    public static CommentDto mapToCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                ItemMapper.mapToItemShortDto(comment.getItem()),
                comment.getAuthor().getName(),
                comment.getCreationDate()
        );
    }
}
