package com.xk.base.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecurityProps props;

    private SecretKey key() {
        String secret = props.getJwt().getSecret();
        byte[] bytes = secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() % 4 == 0
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * ✅ 使用「預設存活分鐘數」來產生 Token
     */
    public String generate(String subject, String[] roles, Map<String, Object> extraClaims) {
        Duration ttl = props.getJwt().getExpiry(); // ✅ 直接用 Duration
        return generate(subject, roles, extraClaims, ttl);
    }


    /**
     * ✅ 可指定 TTL（登入可用 2 小時 / 管理員可用更長）
     */
    public String generate(String subject, String[] roles, Map<String, Object> extraClaims, Duration ttl) {
        Instant now = Instant.now();

        var builder = Jwts.builder()
                .issuer(props.getJwt().getIssuer())
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)));

        if (extraClaims != null)
            extraClaims.forEach(builder::claim);

        return builder.signWith(key(), Jwts.SIG.HS256).compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .clockSkewSeconds(60) // ✅ 建議保留 60s 時鐘容忍
                .verifyWith(key())
                .build()
                .parseSignedClaims(token);
    }

    public Claims parseClaims(String token) {
        return parse(token).getBody();
    }
}
