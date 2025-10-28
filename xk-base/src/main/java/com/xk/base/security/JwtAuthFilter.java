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

                String[] rolesArr = claims.get("roles", String[].class);
                List<String> roles = rolesArr == null ? List.of() : Arrays.asList(rolesArr);

                Collection<? extends GrantedAuthority> authorities = roles.stream().map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));
            } catch (Exception ignore) {
                // token 無效：不設認證，交由 Security 授權規則處理（會得到 401/403）
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
