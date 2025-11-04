package com.xk.truck.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xk.App;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

@SpringBootTest(
        classes = App.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test"
        }
)
class AuthControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper mapper;

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("POST /auth/login 成功後可取得 JWT，且 /auth/me 能正確回傳使用者資訊")
    void login_then_me_ok() throws Exception {
        // UpmsSeedConfig 會建立 admin/admin123
        var loginBody = Map.of("username", "admin", "password", "admin123");
        ResponseEntity<String> loginResp = rest.postForEntity(baseUrl("/auth/login"), loginBody, String.class);

        Assertions.assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 解析出 token
        Map<String,Object> tokenMap = mapper.readValue(loginResp.getBody(), new TypeReference<>(){});
        // 你的 ApiResponseAdvice 會包 ApiResult.success(...)；LoginResponse 也可能直接回傳
        // 因你已在 AuthController return ResponseEntity<LoginResponse>
        // 因此這裡 token 的取法可能是：
        // 1) 沒包 ApiResult 時：直接取 token
        // 2) 有包 ApiResult 時：取 data.token
        String token = null;
        if (tokenMap.containsKey("token")) {
            token = String.valueOf(tokenMap.get("token"));
        } else if (tokenMap.containsKey("data")) {
            Map<?,?> data = (Map<?,?>) tokenMap.get("data");
            token = String.valueOf(data.get("token"));
        }
        Assertions.assertThat(token).isNotBlank();

        // 呼叫 /auth/me
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(headers);
        ResponseEntity<String> meResp = rest.exchange(baseUrl("/auth/me"), HttpMethod.GET, entity, String.class);
        Assertions.assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String,Object> me = mapper.readValue(meResp.getBody(), new TypeReference<>(){});
        // 有包 ApiResult 的情況
        Object dataNode = me.getOrDefault("data", me);
        Map<?,?> data = (Map<?,?>) dataNode;
        Assertions.assertThat(data.get("username")).isEqualTo("admin");
    }

    @Test
    @DisplayName("GET /auth/me 未帶 Token 應 401")
    void me_without_token_unauthorized() {
        ResponseEntity<String> meResp = rest.getForEntity(baseUrl("/auth/me"), String.class);
        Assertions.assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /auth/refresh 使用有效 Token 可換新 Token")
    void refresh_ok() throws Exception {
        // 先登入
        var loginBody = Map.of("username", "admin", "password", "admin123");
        ResponseEntity<String> loginResp = rest.postForEntity(baseUrl("/auth/login"), loginBody, String.class);
        Assertions.assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String,Object> tokenMap = mapper.readValue(loginResp.getBody(), new TypeReference<>(){});
        String token = tokenMap.containsKey("token")
                ? String.valueOf(tokenMap.get("token"))
                : String.valueOf(((Map<?,?>)tokenMap.get("data")).get("token"));

        // 送 refresh
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(headers);
        ResponseEntity<String> refreshResp = rest.exchange(baseUrl("/auth/refresh"), HttpMethod.POST, entity, String.class);
        Assertions.assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String,Object> body = mapper.readValue(refreshResp.getBody(), new TypeReference<>(){});
        Map<?,?> data = (Map<?,?>) body.getOrDefault("data", body);
        Assertions.assertThat(data.get("token")).isNotNull();
    }
}
