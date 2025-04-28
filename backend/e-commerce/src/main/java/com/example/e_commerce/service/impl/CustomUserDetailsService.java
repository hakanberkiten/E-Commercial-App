// CustomUserDetailsService.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.builder()
            .username(u.getEmail())
            .password(u.getPassword())
            .roles(u.getRole().getRoleName())
            .build();
    }
}
