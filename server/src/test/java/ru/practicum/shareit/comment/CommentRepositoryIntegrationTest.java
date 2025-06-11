package ru.practicum.shareit.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private Item item1;
    private Item item2;
    private Comment comment1ForItem1;
    private Comment comment2ForItem1;
    private Comment comment1ForItem2;

    @BeforeEach
    void setUp() {
        user1 = new User(0L, "Commenter User", "commenter@example.com");
        entityManager.persist(user1);

        User owner = new User(0L, "Owner", "owner@example.com");
        entityManager.persist(owner);

        item1 = new Item(0L, "Item With Comments", "Desc1", true, owner, null, List.of(), List.of());
        entityManager.persist(item1);

        item2 = new Item(0L, "Another Item", "Desc2", true, owner, null, List.of(), List.of());
        entityManager.persist(item2);

        comment1ForItem1 = new Comment(null, "First comment for item 1", item1, user1, LocalDateTime.now().minusDays(2));
        entityManager.persist(comment1ForItem1);

        comment2ForItem1 = new Comment(null, "Second comment for item 1", item1, user1, LocalDateTime.now().minusDays(1));
        entityManager.persist(comment2ForItem1);

        comment1ForItem2 = new Comment(null, "Comment for item 2", item2, user1, LocalDateTime.now());
        entityManager.persist(comment1ForItem2);

        entityManager.flush();
    }

    @Test
    void findAllByItemId_whenCommentsExistForItem_shouldReturnCommentsForItem() {
        List<Comment> foundComments = commentRepository.findAllByItemId(item1.getId());

        assertThat(foundComments).hasSize(2);
        assertThat(foundComments).extracting(Comment::getText)
                .containsExactlyInAnyOrder(comment1ForItem1.getText(), comment2ForItem1.getText());
        assertThat(foundComments).allMatch(comment -> comment.getItem().getId() == item1.getId());
    }

    @Test
    void findAllByItemId_whenNoCommentsExistForItem_shouldReturnEmptyList() {
        Item itemWithNoComments = new Item(0L, "Item With No Comments", "Desc3", true, user1, null, List.of(), List.of());
        entityManager.persist(itemWithNoComments);
        entityManager.flush();

        List<Comment> foundComments = commentRepository.findAllByItemId(itemWithNoComments.getId());

        assertThat(foundComments).isEmpty();
    }

    @Test
    void findAllByItemId_whenItemDoesNotExist_shouldReturnEmptyList() {
        List<Comment> foundComments = commentRepository.findAllByItemId(999L);

        assertThat(foundComments).isEmpty();
    }
}