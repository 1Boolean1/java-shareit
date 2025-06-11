package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;

    private User requester;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private ItemRequestCreateDto itemRequestCreateDto;

    @BeforeEach
    void setUp() {
        requester = new User(1L, "Тестовый Пользователь", "test@example.com");
        LocalDateTime createdTime = LocalDateTime.now().minusDays(1).withNano(0);
        itemRequest = new ItemRequest(1L, "Нужен молоток", requester, createdTime, Collections.emptyList());

        itemRequestDto = new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
                itemRequest.getRequester().getId(), itemRequest.getCreated(),
                Collections.emptyList());
        itemRequestCreateDto = new ItemRequestCreateDto("Нужен молоток");
    }

    @Test
    void createRequestWhenValidShouldReturnCreatedRequest() throws Exception {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest ir = invocation.getArgument(0);
            ir.setId(itemRequest.getId());
            ir.setCreated(itemRequest.getCreated());
            return ir;
        });
        when(itemRepository.findByRequestId(anyLong())).thenReturn(Collections.emptyList());


        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", requester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    void createRequestWhenUserNotFoundShouldReturnNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void getRequestsWhenUserExistsShouldReturnUserRequests() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(requester));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(requester.getId()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(itemRequest.getId())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())));
    }

    @Test
    void getRequestsWhenUserNotFoundForIsExistsUserShouldReturnNotFound() throws Exception {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestWhenRequestExistsShouldReturnRequest() throws Exception {
        User ownerUser = new User(5L, "Owner", "owner@mail.com");

        ru.practicum.shareit.item.Item itemEntity = new ru.practicum.shareit.item.Item();
        itemEntity.setId(10L);
        itemEntity.setName("Молоток");
        itemEntity.setOwner(ownerUser);

        ItemResponseDto itemRespDto = new ItemResponseDto(10L, "Молоток", 5L);

        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(this.itemRequest));
        when(itemRepository.findByRequestId(this.itemRequest.getId())).thenReturn(List.of(itemEntity));

        this.itemRequestDto.setItems(List.of(itemRespDto));

        mockMvc.perform(get("/requests/{id}", this.itemRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(this.itemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(this.itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(itemRespDto.getId().intValue())))
                .andExpect(jsonPath("$.items[0].name", is(itemRespDto.getName())))
                .andExpect(jsonPath("$.items[0].ownerId", is(itemRespDto.getOwnerId().intValue())));
    }

    @Test
    void getRequestWhenRequestNotFoundShouldReturnNotFound() throws Exception {
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/requests/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}