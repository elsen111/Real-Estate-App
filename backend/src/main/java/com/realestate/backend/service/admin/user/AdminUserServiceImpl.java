package com.realestate.backend.service.admin.user;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.user.response.UserResponse;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;


    @Override
    public Page<UserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    ) {

        Specification<UserEntity> specification = UserSpecification.withFilter(request);

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toAdminResponse);

    }

    @Override
    public UserResponse getUserById(UUID userId) {
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

        boolean newStatus = !user.getEnabled();

        user.setEnabled(newStatus);

        userRepository.save(user);

        return newStatus
                ? "User has been enabled successfully"
                : "User has been disabled successfully";

    }

    @Override
    @Transactional
    public String assignAdminRoleToUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id " + userId
                )
        );

        if(!user.getEnabled()){
            throw new BusinessException(
                    "This user profile is not enabled. Firstly, activate this profile, then try again."
            );
        }

        RoleEntity adminRoleEntity = roleRepository.findByRoleName(Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name " + Role.ADMIN.name()
                ));

        user.getRoles().add(adminRoleEntity);
        userRepository.save(user);

        return "Admin role successfully assigned to user: " + userId;
    }


}
