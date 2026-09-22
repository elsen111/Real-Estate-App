package com.realestate.backend.service;

import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.AuthMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.security.SecurityConstants;
import com.realestate.backend.service.impl.AuthServiceImpl;
import com.realestate.backend.service.impl.RefreshTokenServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyMapper agencyMapper;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private HttpServletRequest servletRequest;

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
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "User already exists with email: "
                                + request.getEmail().toLowerCase()
                );

        verify(userRepository)
                .existsByEmail("existing@test.com");

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(authMapper);
    }

    @Test
    void registerUser_succeeds_asClient() {
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

        when(refreshTokenService.createRefreshToken(
                savedUser,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
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

        AuthResponse response = service.registerUser(
                request,
                "client",
                servletRequest
        );

        assertThat(response)
                .isNotNull();

        assertThat(mappedUser.getEmail())
                .isEqualTo("new@test.com");

        assertThat(mappedUser.getPasswordHash())
                .isEqualTo("hashed");

        assertThat(mappedUser.getRoles())
                .contains(clientRole);

        verify(userRepository)
                .saveAndFlush(mappedUser);

        verify(refreshTokenService)
                .createRefreshToken(
                        savedUser,
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(savedUser);
    }

    @Test
    void registerUser_assignsLandlordRole_whenTypeIsLandlord() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("landlord@test.com");
        request.setPassword("Password1!");

        UserEntity mappedUser = UserEntity.builder()
                .roles(new HashSet<>())
                .build();

        UserEntity savedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("landlord@test.com")
                .roles(new HashSet<>())
                .build();

        RoleEntity landlordRole = RoleEntity.builder()
                .roleName(Role.LANDLORD)
                .build();

        when(userRepository.existsByEmail("landlord@test.com"))
                .thenReturn(false);

        when(roleRepository.findByRoleName(Role.LANDLORD))
                .thenReturn(Optional.of(landlordRole));

        when(authMapper.toUserEntity(request))
                .thenReturn(mappedUser);

        when(passwordEncoder.encode("Password1!"))
                .thenReturn("hashed");

        when(userRepository.saveAndFlush(mappedUser))
                .thenReturn(savedUser);

        when(refreshTokenService.createRefreshToken(
                savedUser,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "refresh-token",
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
                "landlord",
                servletRequest
        );

        assertThat(mappedUser.getRoles())
                .contains(landlordRole);

        verify(roleRepository)
                .findByRoleName(Role.LANDLORD);
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
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "User already exists with email: "
                                + ownerRequest.getEmail()
                );

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
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Agency already exists with email: "
                                + agencyRequest.getAgencyBusinessEmail()
                );

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

        when(agencyMemberRepository.save(any(AgencyMemberEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(refreshTokenService.createRefreshToken(
                savedOwner,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
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

        AuthResponse response = service.registerAgencyOwner(
                request,
                servletRequest
        );

        assertThat(response)
                .isNotNull();

        assertThat(mappedOwner.getAgency())
                .isEqualTo(savedAgency);

        assertThat(mappedOwner.getRoles())
                .contains(ownerRole);

        ArgumentCaptor<AgencyMemberEntity> captor =
                ArgumentCaptor.forClass(AgencyMemberEntity.class);

        verify(agencyMemberRepository)
                .save(captor.capture());

        AgencyMemberEntity membership =
                captor.getValue();

        assertThat(membership.getAgency())
                .isEqualTo(savedAgency);

        assertThat(membership.getUser())
                .isEqualTo(savedOwner);

        assertThat(membership.getRole())
                .isEqualTo(Role.AGENCY_OWNER);

        assertThat(membership.isActive())
                .isTrue();

        assertThat(membership.getAddedBy())
                .isNull();

        assertThat(membership.getRemovedBy())
                .isNull();

        verify(authMapper)
                .toUserEntity(ownerRequest);

        verify(authMapper)
                .toAgencyEntity(agencyRequest);

        verify(agencyRepository)
                .saveAndFlush(mappedAgency);

        verify(passwordEncoder)
                .encode("Password1!");

        verify(userRepository)
                .saveAndFlush(mappedOwner);

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
    void login_throws_whenAccountDisabledByAuthenticationManager() {
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

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(userRepository);
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
                .hasMessage("Invalid email or password");

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void login_throws_whenUserAccountIsDisabled() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("correct");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
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

        verifyNoInteractions(agencyMemberRepository);
        verifyNoInteractions(refreshTokenService);
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
                .hasMessage("Invalid email or password");

        verifyNoInteractions(agencyMemberRepository);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void login_succeeds_withoutActiveAgencyMembership() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password1!");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .fullName("John Doe")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        RefreshTokenEntity tokenEntity =
                RefreshTokenEntity.builder()
                        .user(user)
                        .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("user@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        when(refreshTokenService.createRefreshToken(
                user,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "refresh-token",
                        tokenEntity
                )
        );

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(null))
                .thenReturn(null);

        AuthResponse response =
                service.login(request, servletRequest);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo("access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("refresh-token");

        assertThat(response.getAgency())
                .isNull();

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(refreshTokenService)
                .createRefreshToken(
                        user,
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void login_throws_whenAgencyIsPending() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password1!");

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .status(AgencyStatus.PENDING)
                .build();

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("owner@test.com")
                .fullName("Agency Owner")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .user(user)
                        .role(Role.AGENCY_OWNER)
                        .active(true)
                        .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Your agency is not approved yet. Please try again after approval."
                );

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_throws_whenAgencyIsRejected() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password1!");

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .status(AgencyStatus.REJECTED)
                .build();

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("owner@test.com")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .agency(agency)
                        .user(user)
                        .role(Role.AGENCY_OWNER)
                        .active(true)
                        .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Your agency is not approved yet. Please try again after approval."
                );

        verify(refreshTokenService, never())
                .createRefreshToken(any(), anyString(), anyString());

        verify(jwtService, never())
                .generateAccessToken(any());
    }

    @Test
    void login_throws_whenAgencyIsRemoved() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password1!");

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .status(AgencyStatus.REMOVED)
                .isDeleted(true)
                .build();

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("owner@test.com")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .agency(agency)
                        .user(user)
                        .role(Role.AGENCY_OWNER)
                        .active(true)
                        .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() ->
                service.login(request, servletRequest)
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Your agency is not approved yet. Please try again after approval."
                );

        verify(refreshTokenService, never())
                .createRefreshToken(any(), anyString(), anyString());

        verify(jwtService, never())
                .generateAccessToken(any());
    }

    @Test
    void login_succeeds_withApprovedAgency() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password1!");

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .status(AgencyStatus.APPROVED)
                .isDeleted(false)
                .build();

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("owner@test.com")
                .fullName("Agency Owner")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .user(user)
                        .role(Role.AGENCY_OWNER)
                        .active(true)
                        .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(userRepository.findByEmailAndDeletedFalse("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        when(refreshTokenService.createRefreshToken(
                user,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "refresh-token",
                        RefreshTokenEntity.builder().build()
                )
        );

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(agency))
                .thenReturn(null);

        AuthResponse response =
                service.login(request, servletRequest);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo("access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("refresh-token");

        verify(agencyMapper)
                .toAgencyOwnerResponse(agency);

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(refreshTokenService)
                .createRefreshToken(
                        user,
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void refreshToken_rotatesTokenAndReturnsNewAccessToken() {
        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("old-refresh-token");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .build();

        RefreshTokenEntity tokenEntity =
                RefreshTokenEntity.builder()
                        .user(user)
                        .build();

        RefreshTokenServiceImpl.CreatedRefreshToken rotatedToken =
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "new-refresh-token",
                        tokenEntity
                );

        when(refreshTokenService.rotateRefreshToken(
                "old-refresh-token",
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(rotatedToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        RefreshTokenResponse response =
                service.refreshToken(
                        request,
                        servletRequest
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo("new-access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("new-refresh-token");

        assertThat(response.getTokenType())
                .isEqualTo(SecurityConstants.TOKEN_PREFIX.trim());

        assertThat(response.getExpiresInSeconds())
                .isEqualTo(3600L);

        verify(refreshTokenService)
                .rotateRefreshToken(
                        "old-refresh-token",
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void logout_revokesRefreshToken() {
        LogoutRequest request =
                new LogoutRequest();

        request.setRefreshToken("refresh-token");

        service.logout(request);

        verify(refreshTokenService)
                .revokeRefreshToken("refresh-token");
    }

    @Test
    void currentUser_throws_whenUserNotFound() {
        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        when(currentUser.getEmail())
                .thenReturn("missing@test.com");

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.currentUser(currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(agencyMemberRepository);
    }

    @Test
    void currentUser_returnsUserWithoutAgency_whenNoActiveMembership() {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .build();

        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        when(currentUser.getEmail())
                .thenReturn("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(null))
                .thenReturn(null);

        AuthResponse response =
                service.currentUser(currentUser);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAgency())
                .isNull();

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(agencyMapper)
                .toAgencyOwnerResponse(null);
    }

    @Test
    void currentUser_returnsActiveAgency_whenMembershipExists() {
        AgencyEntity agency =
                AgencyEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Prime Realty")
                        .build();

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("owner@test.com")
                        .build();

        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .agency(agency)
                        .user(user)
                        .active(true)
                        .role(Role.AGENCY_OWNER)
                        .build();

        when(currentUser.getEmail())
                .thenReturn("owner@test.com");

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(agency))
                .thenReturn(null);

        AuthResponse response =
                service.currentUser(currentUser);

        assertThat(response)
                .isNotNull();

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(agencyMapper)
                .toAgencyOwnerResponse(agency);
    }

    @Test
    void reactivateAccount_enablesUserAndCreatesNewLoginSession() {
        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("disabled@test.com");
        request.setPassword("Password1!");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("disabled@test.com")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        RefreshTokenEntity refreshTokenEntity =
                RefreshTokenEntity.builder()
                        .build();

        RefreshTokenServiceImpl.CreatedRefreshToken createdRefreshToken =
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "new-refresh-token",
                        refreshTokenEntity
                );

        when(userService.enableAccount(request))
                .thenReturn(user);

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        when(refreshTokenService.createRefreshToken(
                user,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(createdRefreshToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(null))
                .thenReturn(null);

        AuthResponse response =
                service.reactivateAccount(
                        request,
                        servletRequest
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo("new-access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("new-refresh-token");

        assertThat(response.getTokenType())
                .isEqualTo(SecurityConstants.TOKEN_PREFIX.trim());

        assertThat(response.getExpiresInSeconds())
                .isEqualTo(3600L);

        verify(userService)
                .enableAccount(request);

        verify(refreshTokenService)
                .createRefreshToken(
                        user,
                        "127.0.0.1",
                        "JUnit-Agent"
                );

        verify(jwtService)
                .generateAccessToken(user);

        verify(userMapper)
                .toSummary(user);

        verify(agencyMapper)
                .toAgencyOwnerResponse(null);
    }

    @Test
    void reactivateAccount_usesAgencyWhenUserHasActiveMembership() {
        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("owner@test.com");
        request.setPassword("Password1!");

        AgencyEntity agency =
                AgencyEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Prime Realty")
                        .build();

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("owner@test.com")
                        .enabled(true)
                        .roles(new HashSet<>())
                        .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .user(user)
                        .agency(agency)
                        .active(true)
                        .role(Role.AGENCY_OWNER)
                        .build();

        when(userService.enableAccount(request))
                .thenReturn(user);

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        when(refreshTokenService.createRefreshToken(
                user,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "refresh-token",
                        RefreshTokenEntity.builder().build()
                )
        );

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(agency))
                .thenReturn(null);

        AuthResponse response =
                service.reactivateAccount(
                        request,
                        servletRequest
                );

        assertThat(response)
                .isNotNull();

        verify(agencyMapper)
                .toAgencyOwnerResponse(agency);

        verify(refreshTokenService)
                .createRefreshToken(
                        user,
                        "127.0.0.1",
                        "JUnit-Agent"
                );
    }

    @Test
    void deactivateAccount_disablesUserAndRevokesAllRefreshTokens() {
        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("Password1!");

        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("user@test.com")
                .enabled(true)
                .build();

        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        when(userService.disableAccount(
                request,
                currentUser
        )).thenReturn(user);

        service.deactivateAccount(
                request,
                currentUser
        );

        verify(userService)
                .disableAccount(
                        request,
                        currentUser
                );

        verify(refreshTokenRepository)
                .deleteAllByUser(user);
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

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setNewPassword("New1!");
        request.setConfirmNewPassword("Different1!");

        CustomUserDetails currentUser =
                CustomUserDetails.from(user);

        assertThatThrownBy(() ->
                service.changePassword(
                        request,
                        currentUser
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Passwords don't match.");

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void changePassword_throws_whenCurrentPasswordIsWrong() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .build();

        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        when(currentUser.getId())
                .thenReturn(userId);

        when(currentUser.getPassword())
                .thenReturn("old-hash");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("WrongPassword");
        request.setNewPassword("NewPassword1!");
        request.setConfirmNewPassword("NewPassword1!");

        when(passwordEncoder.matches(
                "WrongPassword",
                "old-hash"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.changePassword(
                        request,
                        currentUser
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Current password is wrong.");

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "old-hash"
                );

        verify(userRepository, never())
                .save(any(UserEntity.class));

        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void changePassword_updatesPasswordAndRevokesRefreshTokens() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .passwordHash("old-hash")
                .build();

        CustomUserDetails currentUser =
                mock(CustomUserDetails.class);

        when(currentUser.getId())
                .thenReturn(userId);

        when(currentUser.getPassword())
                .thenReturn("old-hash");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("OldPassword1!");
        request.setNewPassword("NewPassword1!");
        request.setConfirmNewPassword("NewPassword1!");

        when(passwordEncoder.matches(
                "OldPassword1!",
                "old-hash"
        )).thenReturn(true);

        when(passwordEncoder.encode("NewPassword1!"))
                .thenReturn("new-hash");

        service.changePassword(
                request,
                currentUser
        );

        assertThat(user.getPasswordHash())
                .isEqualTo("new-hash");

        verify(passwordEncoder)
                .encode("NewPassword1!");

        verify(userRepository)
                .save(user);

        verify(refreshTokenService)
                .revokeAllUserRefreshTokens(userId);
    }

    @Test
    void forgotPassword_generatesOtp_whenUserExists() {
        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail("user@test.com");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .build();

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        service.forgotPassword(request);

        verify(otpService)
                .generateAndSendOtp(user);
    }

    @Test
    void forgotPassword_doesNothing_whenUserDoesNotExist() {
        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail("missing@test.com");

        when(userRepository.findByEmailIgnoreCase("missing@test.com"))
                .thenReturn(Optional.empty());

        service.forgotPassword(request);

        verify(otpService, never())
                .generateAndSendOtp(any());
    }

    @Test
    void resetPassword_throws_whenPasswordsDoNotMatch() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("user@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("DifferentPassword1!");

        assertThatThrownBy(() ->
                service.resetPassword(request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Passwords do not match.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordResetOtpRepository);
    }

    @Test
    void resetPassword_throws_whenUserDoesNotExist() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("missing@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("NewPassword1!");

        when(userRepository.findByEmailIgnoreCase("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.resetPassword(request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or OTP.");

        verify(userRepository)
                .findByEmailIgnoreCase("missing@test.com");
    }

    @Test
    void resetPassword_throws_whenOtpDoesNotExist() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("user@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("NewPassword1!");
        request.setOtp("123456");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .build();

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.resetPassword(request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or OTP.");
    }

    @Test
    void resetPassword_throws_whenOtpIsIncorrect() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("user@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("NewPassword1!");
        request.setOtp("123456");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .build();

        PasswordResetOtpEntity otpEntity =
                PasswordResetOtpEntity.builder()
                        .user(user)
                        .otp("654321")
                        .used(false)
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .build();

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otpEntity));

        assertThatThrownBy(() ->
                service.resetPassword(request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or OTP.");

        verify(userRepository, never())
                .save(any(UserEntity.class));
    }

    @Test
    void resetPassword_throws_whenOtpIsExpired() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("user@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("NewPassword1!");
        request.setOtp("123456");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .build();

        PasswordResetOtpEntity otpEntity =
                PasswordResetOtpEntity.builder()
                        .user(user)
                        .otp("123456")
                        .used(false)
                        .expiresAt(LocalDateTime.now().minusMinutes(1))
                        .build();

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otpEntity));

        assertThatThrownBy(() ->
                service.resetPassword(request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("OTP has expired.");

        verify(userRepository, never())
                .save(any(UserEntity.class));
    }

    @Test
    void resetPassword_updatesPasswordAndInvalidatesOtpAndRefreshTokens() {
        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("user@test.com");
        request.setNewPassword("NewPassword1!");
        request.setConfirmPassword("NewPassword1!");
        request.setOtp("123456");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .passwordHash("old-hash")
                        .build();

        PasswordResetOtpEntity otpEntity =
                PasswordResetOtpEntity.builder()
                        .user(user)
                        .otp("123456")
                        .used(false)
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .build();

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otpEntity));

        when(passwordEncoder.encode("NewPassword1!"))
                .thenReturn("new-hash");

        service.resetPassword(request);

        assertThat(user.getPasswordHash())
                .isEqualTo("new-hash");

        assertThat(otpEntity.getUsed())
                .isTrue();

        verify(passwordEncoder)
                .encode("NewPassword1!");

        verify(userRepository)
                .save(user);

        verify(passwordResetOtpRepository)
                .save(otpEntity);

        verify(passwordResetOtpRepository)
                .deleteByUser(user);

        verify(refreshTokenRepository)
                .deleteAllByUser(user);
    }

    @Test
    void reactivateAccount_doesNotUseInactiveMembership() {
        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("agent@test.com");
        request.setPassword("Password1!");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("agent@test.com")
                        .enabled(true)
                        .roles(new HashSet<>())
                        .build();

        when(userService.enableAccount(request))
                .thenReturn(user);

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        when(refreshTokenService.createRefreshToken(
                user,
                "127.0.0.1",
                "JUnit-Agent"
        )).thenReturn(
                new RefreshTokenServiceImpl.CreatedRefreshToken(
                        "refresh-token",
                        RefreshTokenEntity.builder().build()
                )
        );

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.accessTokenExpiresInSeconds())
                .thenReturn(3600L);

        when(userMapper.toSummary(user))
                .thenReturn(
                        com.realestate.backend.dto.response.AuthUserResponse
                                .builder()
                                .build()
                );

        when(agencyMapper.toAgencyOwnerResponse(null))
                .thenReturn(null);

        AuthResponse response =
                service.reactivateAccount(
                        request,
                        servletRequest
                );

        assertThat(response.getAgency())
                .isNull();

        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(agencyMapper)
                .toAgencyOwnerResponse(null);
    }
}