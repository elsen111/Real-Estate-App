package com.realestate.backend.service;

import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.AuthMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.service.impl.AuthServiceImpl;
import com.realestate.backend.service.impl.RefreshTokenServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyMapper agencyMapper;

    @InjectMocks
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient()
                .when(servletRequest.getHeader("User-Agent"))
                .thenReturn("JUnit-Agent");

        lenient()
                .when(servletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");
    }

    @Test
    void registerUser_throws_whenEmailAlreadyExists() {

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("Existing@Test.com");

        when(userRepository.existsByEmail("existing@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.registerUser(
                        request,
                        "client",
                        servletRequest
                )
        )
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerUser_succeeds_whenEmailIsFree() {

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("Password1!");

        UserEntity mappedUser = UserEntity.builder()
                .roles(new HashSet<>())
                .build();

        RoleEntity clientRole = RoleEntity.builder()
                .roleName(Role.CLIENT)
                .build();

        UserEntity savedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("new@test.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.existsByEmail("new@test.com"))
                .thenReturn(false);

        when(roleRepository.findByRoleName(Role.CLIENT))
                .thenReturn(Optional.of(clientRole));

        when(authMapper.toUserEntity(request))
                .thenReturn(mappedUser);

        when(passwordEncoder.encode("Password1!"))
                .thenReturn("hashed");

        when(userRepository.saveAndFlush(mappedUser))
                .thenReturn(savedUser);

        when(refreshTokenService.createRefreshToken(any(), any(), any()))
                .thenReturn(
                        new RefreshTokenServiceImpl.CreatedRefreshToken(
                                "raw-token",
                                RefreshTokenEntity.builder().build()
                        )
                );

        when(jwtService.generateAccessToken(savedUser))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(savedUser))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(null))
                .thenReturn(null);

        service.registerUser(
                request,
                "client",
                servletRequest
        );

        verify(userRepository).saveAndFlush(mappedUser);
    }

    @Test
    void registerAgencyOwner_throws_whenOwnerEmailAlreadyExists() {

        UserRegisterRequest ownerRequest = UserRegisterRequest.builder()
                .fullName("Owner Name")
                .email("owner@example.com")
                .password("Password1!")
                .build();

        AgencyRegisterRequest agencyRequest = AgencyRegisterRequest.builder()
                .agencyName("Prime Realty")
                .agencyBusinessEmail("info@prime.com")
                .agencyBusinessPhone("+994501112233")
                .agencyCity("Baku")
                .agencyAddress("Nizami St. 10")
                .build();

        AgencyOwnerRegisterRequest request =
                new AgencyOwnerRegisterRequest(
                        ownerRequest,
                        agencyRequest
                );

        when(userRepository.existsByEmail("owner@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.registerAgencyOwner(
                        request,
                        servletRequest
                )
        )
                .isInstanceOf(ConflictException.class);

        verify(userRepository)
                .existsByEmail("owner@example.com");

        verifyNoInteractions(agencyRepository);
        verifyNoInteractions(authMapper);
    }

    @Test
    void registerAgencyOwner_throws_whenAgencyEmailAlreadyExists() {

        UserRegisterRequest ownerRequest = UserRegisterRequest.builder()
                .fullName("Owner Name")
                .email("owner@example.com")
                .password("Password1!")
                .build();

        AgencyRegisterRequest agencyRequest = AgencyRegisterRequest.builder()
                .agencyName("Prime Realty")
                .agencyBusinessEmail("info@prime.com")
                .agencyBusinessPhone("+994501112233")
                .agencyCity("Baku")
                .agencyAddress("Nizami St. 10")
                .build();

        AgencyOwnerRegisterRequest request =
                new AgencyOwnerRegisterRequest(
                        ownerRequest,
                        agencyRequest
                );

        when(userRepository.existsByEmail("owner@example.com"))
                .thenReturn(false);

        when(agencyRepository.existsByEmail("info@prime.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.registerAgencyOwner(
                        request,
                        servletRequest
                )
        )
                .isInstanceOf(ConflictException.class);

        verify(userRepository)
                .existsByEmail("owner@example.com");

        verify(agencyRepository)
                .existsByEmail("info@prime.com");

        verifyNoInteractions(authMapper);
    }

    @Test
    void registerAgencyOwner_succeeds_whenOwnerAndAgencyEmailsAreFree() {

        UserRegisterRequest ownerRequest = UserRegisterRequest.builder()
                .fullName("Owner Name")
                .email("owner@example.com")
                .password("Password1!")
                .phoneNumber("+994501112233")
                .build();

        AgencyRegisterRequest agencyRequest = AgencyRegisterRequest.builder()
                .agencyName("Prime Realty")
                .agencyBusinessEmail("info@prime.com")
                .agencyBusinessPhone("+994125551122")
                .agencyCity("Baku")
                .agencyAddress("Nizami St. 10")
                .build();

        AgencyOwnerRegisterRequest request =
                new AgencyOwnerRegisterRequest(
                        ownerRequest,
                        agencyRequest
                );

        UserEntity mappedOwner = UserEntity.builder()
                .roles(new HashSet<>())
                .build();

        AgencyEntity mappedAgency = AgencyEntity.builder()
                .name("Prime Realty")
                .email("info@prime.com")
                .build();

        AgencyEntity savedAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .email("info@prime.com")
                .build();

        RoleEntity ownerRole = RoleEntity.builder()
                .roleName(Role.AGENCY_OWNER)
                .build();

        UserEntity savedOwner = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Owner Name")
                .email("owner@example.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.existsByEmail("owner@example.com"))
                .thenReturn(false);

        when(agencyRepository.existsByEmail("info@prime.com"))
                .thenReturn(false);

        when(roleRepository.findByRoleName(Role.AGENCY_OWNER))
                .thenReturn(Optional.of(ownerRole));

        when(authMapper.toUserEntity(ownerRequest))
                .thenReturn(mappedOwner);

        when(authMapper.toAgencyEntity(agencyRequest))
                .thenReturn(mappedAgency);

        when(agencyRepository.saveAndFlush(mappedAgency))
                .thenReturn(savedAgency);

        when(passwordEncoder.encode("Password1!"))
                .thenReturn("hashed-password");

        when(userRepository.saveAndFlush(mappedOwner))
                .thenReturn(savedOwner);

        when(refreshTokenService.createRefreshToken(any(), any(), any()))
                .thenReturn(
                        new RefreshTokenServiceImpl.CreatedRefreshToken(
                                "raw-refresh-token",
                                RefreshTokenEntity.builder().build()
                        )
                );

        when(jwtService.generateAccessToken(savedOwner))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(savedOwner))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(savedAgency))
                .thenReturn(null);

        AuthResponse response =
                service.registerAgencyOwner(
                        request,
                        servletRequest
                );

        verify(authMapper).toUserEntity(ownerRequest);
        verify(authMapper).toAgencyEntity(agencyRequest);

        verify(agencyRepository)
                .saveAndFlush(mappedAgency);

        verify(passwordEncoder)
                .encode("Password1!");

        verify(userRepository)
                .saveAndFlush(mappedOwner);

        verify(agencyMemberRepository)
                .save(any(AgencyMemberEntity.class));

        verify(refreshTokenService)
                .createRefreshToken(
                        savedOwner,
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(savedOwner);
    }

    @Test
    void login_throws_whenAccountDisabled() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void login_throws_whenCredentialsAreBad() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_throws_whenUserAccountIsDisabled() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("correct");

        UserEntity user = UserEntity.builder()
                .email("user@test.com")
                .enabled(false)
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("user@test.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void login_throws_whenUserNotFoundAfterAuthentication() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("correct");

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("user@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void changePassword_throws_whenNewPasswordsDontMatch() {

        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("New1!");
        request.setConfirmNewPassword("Different1!");

        CustomUserDetails currentUser =
                CustomUserDetails.from(user);

        assertThatThrownBy(() ->
                service.changePassword(request, currentUser)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("don't match");
    }
}