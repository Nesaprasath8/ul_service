package com.ulavu.Security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil){
        this.jwtUtil=jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            rejectUnauthorized(response, "Missing or malformed Authorization header");
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.getClaims(token);
            String username = claims.getSubject();

            if (username == null || username.isBlank() || !jwtUtil.isTokenValid(token)) {
                rejectUnauthorized(response, "Invalid or expired token");
                return;
            }

            // Make the authenticated identity available to controllers/services
            // further down the chain without re-parsing the token.
            request.setAttribute("authUsername", username);
            request.setAttribute("authRoleId", claims.get("roleId"));

        } catch (Exception e) {
            rejectUnauthorized(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"error\",\"result\":null,\"message\":\"" + message + "\"}");
    }
}
