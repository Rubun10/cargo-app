package com.example.CargoAssign.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/") || uri.startsWith("/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("LOGGED_USER") != null) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtService.isTokenValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }

        request.getSession(true).setAttribute("LOGGED_USER", user);
        return true;
    }
}
