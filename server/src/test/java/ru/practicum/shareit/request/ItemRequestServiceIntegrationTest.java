package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Transactional
public class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();


        user1 = new User(1, "User One", "user1@example.com");
        user2 = new User(2, "User Two", "user2@example.com");

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        entityManager.flush();
        entityManager.clear();

        user1 = userRepository.findById(user1.getId()).orElseThrow();
        user2 = userRepository.findById(user2.getId()).orElseThrow();
    }

    @Test
    void create_shouldCreateAndReturnItemRequestDto() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Need a new laptop");

        ItemRequestDto resultDto = itemRequestService.create(createDto, user1.getId());

        assertNotNull(resultDto.getId());
        assertThat(resultDto.getDescription()).isEqualTo("Need a new laptop");
        assertThat(resultDto.getRequesterId()).isEqualTo(user1.getId());
        assertNotNull(resultDto.getCreated());
        assertThat(resultDto.getItems()).isNotNull().isEmpty();

        ItemRequest persistedRequest = itemRequestRepository.findById(resultDto.getId()).orElse(null);
        assertNotNull(persistedRequest);
        assertThat(persistedRequest.getDescription()).isEqualTo("Need a new laptop");
        assertThat(persistedRequest.getRequester().getId()).isEqualTo(user1.getId());
    }

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Need something");
        long nonExistentUserId = 999L;

        assertThrows(NotFoundException.class, () -> {
            itemRequestService.create(createDto, nonExistentUserId);
        });
    }

    @Test
    void getRequests_whenUserNotFound_shouldThrowNotFoundException() {
        long nonExistentUserId = 999L;

        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequests(nonExistentUserId);
        });
    }

    @Test
    void getRequestsFromOtherUsers_shouldReturnRequestsFromOtherUsersOnly() {
        ItemRequest requestByUser1 = new ItemRequest(1, "User1's own request", user1, LocalDateTime.now().minusHours(3), null);
        itemRequestRepository.save(requestByUser1);

        ItemRequest request1ByUser2 = new ItemRequest(2, "Old request by user2", user2, LocalDateTime.now().minusHours(2), null);
        ItemRequest request2ByUser2 = new ItemRequest(3, "New request by user2", user2, LocalDateTime.now().minusHours(1), null);
        itemRequestRepository.save(request1ByUser2);
        itemRequestRepository.save(request2ByUser2);
        entityManager.flush();
        entityManager.clear();

        user1 = userRepository.findById(user1.getId()).orElseThrow();


        List<ItemRequestDto> result = itemRequestService.getRequestsFromOtherUsers(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("New request by user2");
        assertThat(result.get(1).getDescription()).isEqualTo("Old request by user2");
        assertThat(result).allMatch(dto -> dto.getRequesterId().equals(user2.getId()));
        assertThat(result).noneMatch(dto -> dto.getId().equals(requestByUser1.getId()));
    }

    @Test
    void getRequest_shouldReturnSpecificRequestDtoWithItems() {
        ItemRequest request = new ItemRequest(1, "Specific request", user1, LocalDateTime.now(), null);
        request = itemRequestRepository.save(request);

        Item item = new Item(1, "Item for specific request", "Desc", true, user2, request, null, null);
        itemRepository.save(item);
        entityManager.flush();
        entityManager.clear();

        user1 = userRepository.findById(user1.getId()).orElseThrow();


        ItemRequestDto resultDto = itemRequestService.getRequest(request.getId());

        assertNotNull(resultDto);
        assertThat(resultDto.getId()).isEqualTo(request.getId());
        assertThat(resultDto.getDescription()).isEqualTo("Specific request");
        assertThat(resultDto.getRequesterId()).isEqualTo(user1.getId());
        assertThat(resultDto.getItems()).hasSize(1);
        assertThat(resultDto.getItems().getFirst().getName()).isEqualTo("Item for specific request");
        assertThat(resultDto.getItems().getFirst().getOwnerId()).isEqualTo(user2.getId());
    }

    @Test
    void getRequest_whenRequestNotFound_shouldThrowNotFoundException() {
        long nonExistentRequestId = 999L;
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequest(nonExistentRequestId);
        });
    }
}