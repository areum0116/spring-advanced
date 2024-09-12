package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Nested
    class SaveCommentTest {
        @Test
        void comment_등록_중_할일을_찾지_못해_에러가_발생() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                commentService.saveComment(authUser, todoId, request);
            });

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        void 할일의_담당자가_아니면_예외_발생() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            User anotherUser = new User("email", "password", UserRole.USER);
            ReflectionTestUtils.setField(anotherUser, "id", 2L);
            Todo todo = new Todo("title", "contents", "weather", anotherUser);

            CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.saveComment(authUser, todoId, commentSaveRequest));
            assertEquals("Only todo manager can leave a comment.", exception.getMessage());
        }

        @Test
        void comment를_정상적으로_등록() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "title", "contents", user);
            Comment comment = new Comment(request.getContents(), user, todo);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(commentRepository.save(any())).willReturn(comment);

            // when
            CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

            // then
            assertNotNull(result);
        }
    }

    @Test
    void comment_목록_조회() {
        // given
        long todoId = 1;
        User user = new User("email", "pw", UserRole.USER);
        Todo todo = new Todo("title", "content", "weather", user);
        Comment comment = new Comment("contents", user, todo);
        List<Comment> commentList = List.of(comment);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

        // when
        List<CommentResponse> comments = commentService.getComments(todoId);

        // then
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("contents", comments.get(0).getContents());
    }

}
