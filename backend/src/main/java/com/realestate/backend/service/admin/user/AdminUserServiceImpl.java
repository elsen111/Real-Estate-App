package com.realestate.backend.service.admin.user;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public Page<AdminUserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    ) {

        Specification<UserEntity> specification = UserSpecification.withFilter(request);

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toAdminResponse);

    }

    @Override
    public AdminUserResponse getUserById(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id" + userId
                )
        );

        return userMapper.toAdminResponse(user);
    }

    @Override
    @Transactional
    public String toggleUserStatus(UUID userId) {

        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id" + userId
                )
        );

        boolean newStatus = !user.isEnabled();

        user.setEnabled(newStatus);

        userRepository.save(user);

        return newStatus
                ? "User has been enabled successfully"
                : "User has been disabled successfully";

    }
}
