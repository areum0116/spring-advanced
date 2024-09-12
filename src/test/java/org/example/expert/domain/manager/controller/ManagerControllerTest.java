package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.controller.CommentController;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ManagerService managerService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ManagerController(managerService, jwtUtil)).setCustomArgumentResolvers(authUserArgumentResolver).build();
    }

    @Test
    void 매니저_저장() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "aaa@email.com", UserRole.USER);
        long todoId = 1L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(2L, new UserResponse(1L, "aaa@email.com"));
        String requestString = objectMapper.writeValueAsString(managerSaveRequest);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authUser);

        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class))).willReturn(managerSaveResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(managerSaveResponse.getId()))
                .andExpect(jsonPath("$.user.id").value(managerSaveResponse.getUser().getId()));
    }

    @Test
    void 매니저_전체_검색() throws Exception {
        // given
        long todoId = 1L;
        given(managerService.getManagers(todoId)).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}/managers", todoId));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void 매니저_삭제() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 2L;
        AuthUser authUser = new AuthUser(3L, "email@email.com", UserRole.USER);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authUser);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId));

        // then
        resultActions.andExpect(status().isOk());
    }
}