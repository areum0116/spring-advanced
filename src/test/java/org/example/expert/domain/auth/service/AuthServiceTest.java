package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입_정상_등록() {
        // given
        long userId = 1L;
        SignupRequest signupRequest = new SignupRequest("aa@aa.com", "password", "USER");

        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        UserRole userRole = UserRole.of(signupRequest.getUserRole());
        User newUser = new User(signupRequest.getEmail(), "encodedPassword", userRole);
        ReflectionTestUtils.setField(newUser, "id", userId);

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("BearerToken");

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        assertNotNull(signupResponse);
        assertEquals("BearerToken", signupResponse.getBearerToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 회원가입_중복_이메일로_실패() {
        // given
        SignupRequest signupRequest = new SignupRequest("aa@aa.com", "password", "USER");

        // when
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    void 로그인_성공() {
        // given


        // when


        // then


    }

    @Test
    void 로그인_존재하지_않는_유저로_실패() {
        // given


        // when


        // then


    }

    @Test
    void 로그인_틀린_비밀번호로_실패() {
        // given


        // when


        // then


    }
}