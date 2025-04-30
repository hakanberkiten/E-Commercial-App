package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        String roleName;
        switch (user.getRole().getRoleId()) {
            case 1: roleName = "ROLE_ADMIN"; break;
            case 2: roleName = "ROLE_SELLER"; break;
            case 3: roleName = "ROLE_CUSTOMER"; break;
            default: roleName = "ROLE_USER";
        }
        
        // Add detailed logging
        logger.info("User {} has role: {} (ID: {})", email, roleName, user.getRole().getRoleId());
        
        // Check if user is active
        if (!user.getActive()) {
            logger.warn("User {} is inactive, denying authentication", email);
            throw new DisabledException("User account is disabled");
        }
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getActive(), // Account enabled/disabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}