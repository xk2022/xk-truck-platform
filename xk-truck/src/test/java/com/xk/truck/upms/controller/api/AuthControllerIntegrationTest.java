package com.xk.truck.upms.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xk.App;
import com.xk.truck.upms.controller.api.dto.auth.LoginReq;
import com.xk.truck.upms.domain.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = App.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate rest; // ✅ 由 Spring 提供，含 Jackson 轉換

    @Autowired
    SecurityFilterChain securityFilterChain;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    private String token;

    @Test
    @Order(1)
    @DisplayName("✅ 登入成功 - 正確帳密")
    void login_ok() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);
        ResponseEntity<Map> res = rest.postForEntity(baseUrl() + "/login", entity, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKey("token");

        token = (String) res.getBody().get("token");
        System.out.println("🟢 JWT Token = " + token);
    }

    @Test
    @Order(2)
    @DisplayName("🔑 取得當前使用者資訊（含 token）")
    void getMe_ok() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = rest.exchange(
                baseUrl() + "/me", HttpMethod.GET, entity, Map.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("username", "admin");
        System.out.println("✅ User info: " + res.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("🚫 登入失敗 - 密碼錯誤")
    void login_fail() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("wrongpass");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);
        ResponseEntity<Map> res = rest.postForEntity(baseUrl() + "/login", entity, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    @DisplayName("🚫 取得使用者資訊 - 無效 Token")
    void getMe_fail_invalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("INVALID_TOKEN");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = rest.exchange(
                baseUrl() + "/me", HttpMethod.GET, entity, Map.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
