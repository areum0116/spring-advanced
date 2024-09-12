package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TodoService todoService;
    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TodoController(todoService)).setCustomArgumentResolvers(authUserArgumentResolver).build();
        authUser = new AuthUser(1L, "email@email.com", UserRole.USER);
    }

    @Test
    void 할일_저장() throws Exception {
        // given
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        UserResponse userResponse = new UserResponse(1L, "email@email.com");
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L, "title", "contents", "weather", userResponse);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authUser);

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(todoSaveResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/todos")
                .content(objectMapper.writeValueAsString(todoSaveRequest))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoSaveResponse.getId()))
                .andExpect(jsonPath("$.title").value(todoSaveResponse.getTitle()))
                .andExpect(jsonPath("$.contents").value(todoSaveResponse.getContents()))
                .andExpect(jsonPath("$.weather").value(todoSaveResponse.getWeather()))
                .andExpect(jsonPath("$.user.email").value(authUser.getEmail()));
    }

    @Test
    void 할일_가져오기() throws Exception {
        // given
        long todoId = 1L;
        TodoResponse todoResponse = new TodoResponse(1L, "title", "contents", "weather", null, null, null);

        given(todoService.getTodo(anyLong())).willReturn(todoResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}", todoId));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoResponse.getId()))
                .andExpect(jsonPath("$.title").value(todoResponse.getTitle()))
                .andExpect(jsonPath("$.contents").value(todoResponse.getContents()));
    }
}