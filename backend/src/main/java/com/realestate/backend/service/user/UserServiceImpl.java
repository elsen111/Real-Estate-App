package com.realestate.backend.service.user;

import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.dto.user.request.DeleteAccountRequest;
import com.realestate.backend.dto.user.request.UpdateProfileRequest;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.RefreshTokenRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.realestate.backend.enums.Role.AGENCY_OWNER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private  final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public UserResponse updateProfile(
            UpdateProfileRequest request,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with this email"));

        updateFullName(user, request);
        updateEmail(user, request);
        updatePhoneNumber(user, request);

        user = userRepository.save(user);

        return userMapper.toSummary(user);

    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findByEmail(
                currentUser.getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "Incorrect password provided"
            );
        }

//        if (user.getRoles().contains(AGENCY_OWNER)) {
//            throw new ConflictException(
//                    "Agency owners cannot delete their account. Transfer ownership or ask admin to deactivate your agency first."
//            );
//        }

        user.setEnabled(false);

        userRepository.save(user);

        refreshTokenRepository.deleteAllByUser(user);

    }

    private void updatePhoneNumber(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

    }

    private void updateEmail(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if (request.getEmail() == null) {
            return;
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if(email.equals(user.getEmail())) {
            return;
        }

        if(userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "User already exists with this email"
            );
        }

        user.setEmail(email);

    }

    private void updateFullName(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if(request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

    }
}
