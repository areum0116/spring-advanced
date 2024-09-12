package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Nested
    class GetTodoTest {
        @Test
        void 일정_조회_entity_없음() {
            // given
            long todoId = 1L;
            long userId = 2L;
            User user = new User("aa@aa.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = new Todo("title", "content", "weather", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            when(todoRepository.findByIdWithUser(anyLong())).thenReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        void 일정_조회_성공() {
            // given
            long todoId = 1L;
            long userId = 2L;
            User user = new User("aa@aa.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = new Todo("title", "content", "weather", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

            // when
            TodoResponse findTodo = todoService.getTodo(todoId);

            // then
            assertNotNull(findTodo);
            assertEquals(todoId, findTodo.getId());

        }
    }

    @Test
    void 일정_저장() {
        // given
        AuthUser authUser = new AuthUser(1L, "email@naver.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest request = new TodoSaveRequest("title", "content");
        String weather = "weather";
        given(weatherClient.getTodayWeather()).willReturn(weather);
        Todo todo = new Todo(request.getTitle(), request.getContents(), weather, user);

        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, request);

        // then
        assertEquals(todo.getId(), todoSaveResponse.getId());
        assertEquals(todo.getTitle(), todoSaveResponse.getTitle());
        assertEquals(todo.getContents(), todoSaveResponse.getContents());
        assertEquals(todo.getWeather(), todoSaveResponse.getWeather());
    }

    @Test
    void 일정_전체_조회() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);
        Todo todo = new Todo("title", "content", "weather", new User("email", "pw", UserRole.USER));
        Page<Todo> todos = new PageImpl<>(List.of(todo), pageable, 1);

        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todos);

        // when
        Page<TodoResponse> findTodos = todoService.getTodos(page, size);

        // then
        assertNotNull(findTodos);
        assertEquals(todos.getTotalElements(), findTodos.getTotalElements());
    }
}