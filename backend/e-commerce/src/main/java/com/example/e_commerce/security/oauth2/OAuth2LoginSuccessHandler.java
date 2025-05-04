package com.example.e_commerce.security.oauth2;

import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.security.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();
        
        System.out.println("OAuth2 login success for email: " + email);
        
        if (email == null) {
            System.err.println("Error: No email provided by OAuth provider");
            response.sendRedirect("http://localhost:4200/login?error=no_email");
            return;
        }
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isEmpty()) {
            // User doesn't exist yet, create a new one
            System.out.println("Creating new user from OAuth2 login");
            user = User.builder()
                .email(email)
                .firstName(oAuth2User.getFirstName() != null ? oAuth2User.getFirstName() : "Google")
                .lastName(oAuth2User.getLastName() != null ? oAuth2User.getLastName() : "User")
                .authProvider("GOOGLE")
                .providerId(oAuth2User.getProviderId())
                .active(true)
                // Generate a secure random password - not needed for OAuth login but required by schema
                .password(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()))
                // Default mobile number (can be updated later by the user)
                .mobileNumber("0000000000")
                .build();
            
            // Set default CUSTOMER role (roleId = 3)
            Role customerRole = roleRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER not found."));
            user.setRole(customerRole);
            
            user = userRepository.save(user);
            System.out.println("New user created with ID: " + user.getUserId());
        } else {
            // User exists, update OAuth info if needed
            user = userOptional.get();
            System.out.println("Found existing user: " + user.getEmail());
            if (user.getAuthProvider() == null) {
                user.setAuthProvider("GOOGLE");
                user.setProviderId(oAuth2User.getProviderId());
                userRepository.save(user);
                System.out.println("Updated existing user with OAuth info");
            }
        }
        
        // Generate JWT token
        String token = jwtUtils.generateToken(user.getEmail());
        
        // Redirect to the frontend with the token
        String redirectUrl = "http://localhost:4200/auth/oauth2/success?token=" + token;
        System.out.println("OAuth2 redirect to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
