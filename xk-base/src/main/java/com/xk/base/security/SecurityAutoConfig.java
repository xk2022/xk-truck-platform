package com.xk.base.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableConfigurationProperties(SecurityProps.class)
@RequiredArgsConstructor
@EnableMethodSecurity // ✅ 支援 @PreAuthorize
public class SecurityAutoConfig {

    private final SecurityProps props;

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        return new JwtAuthFilter(jwtService, props.getPermitAll());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            ObjectProvider<AuthenticationEntryPoint> entryPointOp,
            ObjectProvider<AccessDeniedHandler> deniedHandlerOp
    ) throws Exception {

        http
                // Stateless JWT
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 授權規則
                .authorizeHttpRequests(auth -> {
                    // Swagger / Actuator / Error / Login 放行
                    auth.requestMatchers(
                            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                            "/actuator/health", "/error", "/auth/login"
                    ).permitAll();

                    // YAML 白名單（防 null）
                    List<String> wl = props.getPermitAll() != null ? props.getPermitAll() : List.of();
                    wl.forEach(p -> auth.requestMatchers(p).permitAll());

                    // Preflight
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // 範例：GET /hello 放行（保留或移除皆可）
                    auth.requestMatchers(HttpMethod.GET, "/hello").permitAll();

                    // 其餘需驗證
                    auth.anyRequest().authenticated();
                })

                // 關閉框架內建互動式登入/登出/httpBasic
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .httpBasic(b -> b.disable())

                // JWT Filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 有提供自訂 Handler 就掛上，沒有就不設定
        AuthenticationEntryPoint ep = entryPointOp.getIfAvailable();
        AccessDeniedHandler adh = deniedHandlerOp.getIfAvailable();
        if (ep != null || adh != null) {
            http.exceptionHandling(e -> {
                if (ep != null) e.authenticationEntryPoint(ep);
                if (adh != null) e.accessDeniedHandler(adh);
            });
        }

        return http.build();
    }

    // CORS：若需要帶憑證，請把 allowedOrigins 換成 allowedOriginPatterns
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 若要支援 cookie/authorization，請啟用下一行並改用 allowedOriginPatterns
        // cfg.setAllowCredentials(true);
        // cfg.setAllowedOriginPatterns(props.getCors().getAllowedOrigins()); // 若使用 patterns

        cfg.setAllowedOrigins(props.getCors().getAllowedOrigins());
        cfg.setAllowedMethods(props.getCors().getAllowedMethods());
        cfg.setAllowedHeaders(props.getCors().getAllowedHeaders());
        cfg.setExposedHeaders(props.getCors().getExposedHeaders());
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // 預設使用者（可被上層覆蓋）
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        UserDetails dispatcher = User.withUsername("dispatcher")
                .password(encoder.encode("dispatcher123"))
                .roles("DISPATCH")
                .build();
        return new InMemoryUserDetailsManager(admin, dispatcher);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
