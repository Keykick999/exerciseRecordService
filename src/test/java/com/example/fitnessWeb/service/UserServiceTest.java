//package com.example.fitnessWeb.service;
//
//import com.example.fitnessWeb.dto.UserLoginRequest;
//import com.example.fitnessWeb.dto.UserRegisterRequest;
//import com.example.fitnessWeb.entity.User;
//import com.example.fitnessWeb.repository.UserRepository;
//import com.example.fitnessWeb.config.JwtUtil;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import java.util.Optional;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @InjectMocks
//    private UserService userService;
//
//    @Test
//    void 회원가입_성공_저장된_값_검증() {
//        // given
//        UserRegisterRequest request = new UserRegisterRequest("tom", "1234", "tom@email.com");
//
//        when(userRepository.existsByUsername("tom")).thenReturn(false);
//        when(userRepository.existsByEmail("tom@email.com")).thenReturn(false);
//        when(passwordEncoder.encode("1234")).thenReturn("hashedPassword");
//
//        // when
//        userService.register(request);
//
//        // then
//        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
//        verify(userRepository).save(userCaptor.capture());
//
//        User savedUser = userCaptor.getValue();
//        assertEquals("tom", savedUser.getUsername());
//        assertEquals("hashedPassword", savedUser.getPassword());
//        assertEquals("tom@email.com", savedUser.getEmail());
//    }
//
//
//    @Test
//    void 회원가입_실패_중복_사용자명() {
//        // given
//        UserRegisterRequest request = new UserRegisterRequest("tom", "1234", "tom@email.com");
//        when(userRepository.existsByUsername("tom")).thenReturn(true);
//
//        // when & then
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.register(request);
//        });
//
//        assertEquals("이미 존재하는 사용자 이름입니다.", exception.getMessage());
//    }
//
//
//    @Test
//    void 회원가입_실패_중복_이메일() {
//        // given
//        UserRegisterRequest request = new UserRegisterRequest("uniqueUsername", "1234", "tom@email.com");
//
//        when(userRepository.existsByUsername("uniqueUsername")).thenReturn(false);
//        when(userRepository.existsByEmail("tom@email.com")).thenReturn(true);
//
//        // when & then
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.register(request);
//        });
//
//        assertEquals("이미 등록된 이메일입니다.", exception.getMessage());
//    }
//
//
//
//    @Test
//    void 로그인_성공() {
//        // given
//        UserLoginRequest request = new UserLoginRequest("tom", "1234");
//        User user = User.builder()
//                .username("tom")
//                .password("hashedPassword")
//                .email("tom@email.com")
//                .build();
//
//        when(userRepository.findByUsername("tom")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("1234", "hashedPassword")).thenReturn(true);
//        when(jwtUtil.generateToken("tom")).thenReturn("mocked-jwt-token");
//
//        // when
//        String token = userService.login(request);
//
//        // then
//        assertEquals("mocked-jwt-token", token);
//    }
//
//
//    @Test
//    void 로그인_실패_존재하지_않는_사용자명() {
//        // given
//        UserLoginRequest request = new UserLoginRequest("unknownUser", "1234");
//        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());
//
//        // when & then
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.login(request);
//        });
//
//        assertEquals("존재하지 않는 사용자입니다.", exception.getMessage());
//    }
//
//
//    @Test
//    void 로그인_실패_비밀번호_불일치() {
//        // given
//        UserLoginRequest request = new UserLoginRequest("tom", "wrongPassword");
//        User user = User.builder()
//                .id(1L)
//                .username("tom")
//                .password("hashedPassword")
//                .email("tom@email.com")
//                .build();
//
//
//        // DB에서 사용자 존재 → 조회 성공
//        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
//
//        // 비밀번호 불일치
//        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);
//
//
//        // when
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//           userService.login(request);
//        });
//
//
//        // then
//        assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
//    }
//}
