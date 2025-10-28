package com.xk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 啟動應用後打開：
 * Swagger UI：http://localhost:8080/swagger-ui/index.html
 * OpenAPI JSON：http://localhost:8080/v3/api-docs
 * 在 Swagger UI 右上角點 Authorize，輸入：Bearer <你的JWT>，即可測試受保護 API。
 */
@SpringBootApplication(scanBasePackages = {"com.xk"})
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(App.class, args);
    }

}
