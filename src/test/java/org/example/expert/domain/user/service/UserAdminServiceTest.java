package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void 유저_권한_변경_성공() {
        // given
        long userId = 1L;
        User user = new User("email", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, userRoleChangeRequest);

        // then
        verify(userRepository, times(1)).findById(userId);
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    void 유저를_찾지_못할시_예외_발생() {
        // given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("USER");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userAdminService.changeUserRole(userId, userRoleChangeRequest));
        assertEquals("User not found", exception.getMessage());
    }
}