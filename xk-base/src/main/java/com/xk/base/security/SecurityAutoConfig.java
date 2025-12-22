package com.xk.base.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityAutoConfig {

    private final SecurityProps props;
    private final JwtAuthFilter jwtAuthFilter;

    // somewhere in your security config (xk-truck 的 TruckAuthAdapter 或 xk-base 的 SecurityAutoConfig)
    // 建議只留一個地方定義，另一邊用 @ConditionalOnMissingBean
    @Bean
    @ConditionalOnMissingBean   // << 沒有 PasswordEncoder 才提供
    public PasswordEncoder passwordEncoder() {
        // 預設使用 bcrypt，會產生形如 {bcrypt}$2a... 的字串
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // 會支援 {bcrypt}、{noop}... 等前綴；DB 有 {bcrypt} 就會自動走 BCrypt。

    @Bean(name = "xkDaoAuthenticationProvider")
    @ConditionalOnMissingBean(DaoAuthenticationProvider.class) // << 沒有任何 DaoAuthenticationProvider 才提供
    public DaoAuthenticationProvider xkDaoAuthenticationProvider(
            PasswordEncoder encoder,
            UserDetailsService userDetailsService
    ) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(userDetailsService);
        return p;
    }

    @Bean
    @ConditionalOnMissingBean   // << 沒有 AuthenticationManager 才提供
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class) // << 沒有任何 SecurityFilterChain 才提供預設
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<DaoAuthenticationProvider> daoProviderOp) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(props.getPermitAll().toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 如果外部有提供 DaoAuthenticationProvider，就掛上去
        DaoAuthenticationProvider dao = daoProviderOp.getIfAvailable();
        if (dao != null) {
            http.authenticationProvider(dao);
        }
        return http.build();
    }

    public static void main(String[] args) {
        var enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        System.out.println(enc.encode("admin123"));
        // 複製輸出，長得像 {bcrypt}$2a$10$xxxxx...
        // 把你要測的純文字密碼放這裡，像 admin123
        String raw = "admin123";
        // 把 DB 裡 admin 的雜湊貼進來（含 {bcrypt} 前綴）
        String hash = "{bcrypt}$2a$10$1iibUrc5UIiOssaHmpgoAueWcXskeqQnTeu0ZqfAhtrx9u1U6KBuS";
        System.out.println(enc.matches(raw, hash)); // true 才表示密碼正確
    }
}
