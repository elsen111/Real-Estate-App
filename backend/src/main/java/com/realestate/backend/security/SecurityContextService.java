package com.realestate.backend.security;

import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityContextService {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found."
                        )
                );
    }
}