package com.xk.base.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final List<String> permitPrefixes;

    public JwtAuthFilter(JwtService jwtService, List<String> permitPrefixes) {
        this.jwtService = jwtService;
        this.permitPrefixes = permitPrefixes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtService.parseClaims(token);   // ✅ 0.13.0：getBody() 取 Claims
                String username = claims.getSubject();

                Object raw = claims.get("roles");
                List<String> roles =
                        (raw instanceof java.util.List<?> l) ? l.stream().map(Object::toString).toList()
                                : (raw instanceof String s) ? java.util.Arrays.asList(s.split(","))
                                : java.util.List.of();

                // ✅ 只補一次 ROLE_
                Collection<? extends GrantedAuthority> authorities = roles.stream()
                        .filter(r -> r != null && !r.isBlank())
                        .map(String::trim)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 🔎 關鍵日誌
                System.out.println("[JWT] user=" + username + " roles=" + roles + " authorities=" + authorities);

                var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // token 無效：不設認證，交由 Security 授權規則處理（會得到 401/403）
                System.out.println("[JWT] parse failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return permitPrefixes.stream().anyMatch(p -> {
            String prefix = p.replace("/**", "");
            return path.startsWith(prefix);
        });
    }
}
