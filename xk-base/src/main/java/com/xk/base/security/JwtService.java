package com.xk.base.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecurityProps props;

    private SecretKey key() {
        // 支援「原字串」或「Base64」兩種 secret 來源
        String secret = props.getJwt().getSecret();
        byte[] bytes = secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() % 4 == 0 ? Decoders.BASE64.decode(secret) : secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 產 Token：roles 以 String[] 寫入，避免泛型轉型問題
     */
    public String generate(String subject, String[] roles, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        var builder = Jwts.builder().issuer(props.getJwt().getIssuer()).subject(subject).claim("roles", roles).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(props.getJwt().getExpiryMinutes() * 60)));

        if (extraClaims != null) extraClaims.forEach(builder::claim);

        return builder.signWith(key(), Jwts.SIG.HS256)   // ✅ 0.13.0 用法
                .compact();
    }

    /**
     * 解析並驗簽，回傳 Jws<Claims>
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key())                  // ✅ 0.13.0 用法
                .build().parseSignedClaims(token);
    }

    /**
     * 方便使用：直接拿 Claims
     */
    public Claims parseClaims(String token) {
        return parse(token).getBody();             // ✅ 用 getBody()
    }
}
