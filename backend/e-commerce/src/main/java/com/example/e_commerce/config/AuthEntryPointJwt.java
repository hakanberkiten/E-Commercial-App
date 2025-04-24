package com.example.e_commerce.config;


import jakarta.servlet.http.*;
import org.springframework.security.core.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;

public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}
