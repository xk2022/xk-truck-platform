package com.xk.truck.debug;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugWhoAmI {

    @GetMapping("/_me")
    public Map<String, Object> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "principal", auth == null ? null : auth.getName(),
                "authorities", auth == null ? null : auth.getAuthorities()
        );
    }
}
