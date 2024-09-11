package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Nested
    class UserTest {
        @Test
        void user_없으면_예외_발생() {
            // given
            long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        void user_정상_조회() {
            // given
            long userId = 1L;
            User user = new User("email", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse userResponse = userService.getUser(userId);

            // then
            assertNotNull(userResponse);
            assertEquals(userId, userResponse.getId());
            assertEquals("email", userResponse.getEmail());
        }
    }

    @Nested
    class PasswordTest {
        @Test
        void 비밀번호_변경_성공() {
            // given
            long userId = 1L;
            String oldPassword = "Aaaa1111*";
            String newPassword = "Aaaa2222*";
            User user = new User("email", oldPassword, UserRole.USER);

            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            given(passwordEncoder.matches(newPassword, user.getPassword())).willReturn(false);
            given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(true);
            given(passwordEncoder.encode(userChangePasswordRequest.getNewPassword())).willReturn("encodedNewPassword");

            // when
            userService.changePassword(userId, userChangePasswordRequest);

            // then
            verify(userRepository, times(1)).findById(userId);
            verify(passwordEncoder, times(2)).matches(anyString(), anyString());
            verify(passwordEncoder, times(1)).encode(anyString());
            assertEquals("encodedNewPassword", user.getPassword());
        }

        @Test
        void 같은_비밀번호로_변경시_예외_발생() {
            // given
            long userId = 1L;
            User user = new User("email", passwordEncoder.encode("Aaaa1111*"), UserRole.USER);
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("Aaaa1111*", "Aaaa1111*");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, userChangePasswordRequest));

            // then
            assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
        }
    }
}