package com.xk.base.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityProps props;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (props.getPermitAll().stream().anyMatch(uri::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtService.parse(header.substring(7)).getPayload();
            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
