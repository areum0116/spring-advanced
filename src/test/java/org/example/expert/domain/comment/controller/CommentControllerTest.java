package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private CommentService commentService;
    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService)).setCustomArgumentResolvers(authUserArgumentResolver).build();
    }

    @Test
    void 댓글_조회() throws Exception {
        // given
        long todoId = 1L;
        given(commentService.getComments(anyLong())).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}/comments", todoId));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void 댓글_등록() throws Exception {
        // given
        long todoId = 1L;
        AuthUser authUser = new AuthUser(1L, "email@a.com", UserRole.USER);
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(1L, "contents", new UserResponse(1L, "email@a.com"));

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authUser);

        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class))).willReturn(commentSaveResponse);
        String body = mapper.writeValueAsString(commentSaveRequest);

        // when
        ResultActions resultActions = mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentSaveResponse.getId()))
                .andExpect(jsonPath("$.contents").value(commentSaveResponse.getContents()))
                .andExpect(jsonPath("$.user.email").value(commentSaveResponse.getUser().getEmail()));
    }

}