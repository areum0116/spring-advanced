package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.controller.TodoController;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).setCustomArgumentResolvers(authUserArgumentResolver).build();
    }

    @Test
    void 유저_가져오기() throws Exception {
        // given
        long userId = 1L;
        UserResponse userResponse = new UserResponse(1L, "email@email.com");
        given(userService.getUser(anyLong())).willReturn(userResponse);

        // when
        ResultActions resultActions = mvc.perform(get("/users/{userId}", userId));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));
    }

    @Test
    void 비밀번호_변경() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword");

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authUser);

        doNothing().when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));

        // when
        ResultActions resultActions = mvc.perform(put("/users")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
    }
}