package com.realestate.backend.service.auth;

import com.realestate.backend.dto.auth.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.auth.request.*;
import com.realestate.backend.dto.auth.response.AuthResponse;
import com.realestate.backend.dto.auth.response.RefreshTokenResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.mapper.auth.AuthMapper;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.security.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;

    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final AgencyMapper agencyMapper;

    @Transactional
    public AuthResponse registerUser(
            UserRegisterRequest request,
            String type,
            HttpServletRequest servletRequest
    ) {
        String email = normalizeEmail(request.getEmail());

        ensureUserEmailIsFree(email);

        Role role = "landlord".equalsIgnoreCase(type) ? Role.LANDLORD : Role.CLIENT;
        RoleEntity clientRole = getRole(role);

        UserEntity user = authMapper.toUserEntity(request);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(clientRole);

        user = userRepository.saveAndFlush(user);

        RefreshTokenService.CreatedRefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user,
                        extractIpAddress(servletRequest),
                        servletRequest.getHeader("User-Agent")
                );

        return buildAuthResponse(user, refreshToken.rawToken(), null);
    }

    @Transactional
    public AuthResponse registerAgencyOwner(
            AgencyOwnerRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        String ownerEmail = normalizeEmail(request.getEmail());

        ensureUserEmailIsFree(ownerEmail);

        RoleEntity agencyAdminRole = getRole(Role.AGENCY_OWNER);

        UserEntity owner = authMapper.toAgencyOwnerUser(request);
        AgencyEntity agency = authMapper.toAgencyEntity(request);
        agency = agencyRepository.saveAndFlush(agency);

        owner.setEmail(ownerEmail);
        owner.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        owner.getRoles().add(agencyAdminRole);
        owner.setAgency(agency);

        owner = userRepository.saveAndFlush(owner);

        AgencyMemberEntity ownerMembership = AgencyMemberEntity.builder()
                .agency(agency)
                .user(owner)
                .active(true)
                .build();

        agencyMemberRepository.save(ownerMembership);

        RefreshTokenService.CreatedRefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        owner,
                        extractIpAddress(servletRequest),
                        servletRequest.getHeader("User-Agent")
                );

        return buildAuthResponse(owner, refreshToken.rawToken(), agency);
    }

    @Transactional
    public AuthResponse login(
            LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String email = normalizeEmail(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("User account is disabled");
        }

        AgencyMemberEntity membership = agencyMemberRepository
                .findByUserAndActiveTrue(user)
                .orElse(null);
        AgencyEntity agency = membership != null ? membership.getAgency() : null;

        RefreshTokenService.CreatedRefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user,
                        extractIpAddress(servletRequest),
                        servletRequest.getHeader("User-Agent")
                );

        return buildAuthResponse(user, refreshToken.rawToken(), agency);
    }

    @Transactional
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        RefreshTokenService.CreatedRefreshToken rotatedToken =
                refreshTokenService.rotateRefreshToken(
                        request.getRefreshToken(),
                        extractIpAddress(servletRequest),
                        servletRequest.getHeader("User-Agent")
                );

        UserEntity user = rotatedToken.entity().getUser();

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(rotatedToken.rawToken())
                .tokenType(SecurityConstants.TOKEN_PREFIX.trim())
                .expiresInSeconds(jwtService.accessTokenExpiresInSeconds())
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
    }

    @Transactional(readOnly = true)
    public AuthResponse currentUser(CustomUserDetails currentUser) {
        UserEntity user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AgencyMemberEntity membership = agencyMemberRepository
                .findByUserAndActiveTrue(user)
                .orElse(null);

        AgencyEntity agency = membership != null
                ? membership.getAgency()
                : null;

        return AuthResponse.builder()
                .user(userMapper.toSummary(user))
                .agency(agencyMapper.toSummary(agency))
                .build();
    }

    private AuthResponse buildAuthResponse(
            UserEntity user,
            String refreshToken,
            AgencyEntity agency
    ) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.TOKEN_PREFIX.trim())
                .expiresInSeconds(jwtService.accessTokenExpiresInSeconds())
                .user(userMapper.toSummary(user))
                .agency(agencyMapper.toSummary(agency))
                .build();
    }

    private RoleEntity getRole(Role role) {
        return roleRepository.findByRoleName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));
    }

    private void ensureUserEmailIsFree(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User already exists with this email");
        }
    }

    private void ensureAgencyEmailIsFree(String email) {
        if (agencyRepository.existsByEmail(email)) {
            throw new ConflictException("Agency already exists with this email");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}