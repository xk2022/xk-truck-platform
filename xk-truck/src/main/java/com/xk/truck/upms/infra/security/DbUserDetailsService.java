package com.xk.truck.upms.infra.security;

import com.xk.truck.upms.domain.dao.repository.UserRepository;

import com.xk.truck.upms.domain.model.po.User;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String[] authorities = u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode()) // 你的 Permission code = authority
                .distinct()
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .disabled(!u.getEnabled())
                .accountLocked(Boolean.TRUE.equals(u.getLocked()))
                .authorities(authorities)
                .build();
    }
}
