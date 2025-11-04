package com.xk.truck.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroups {

    @Bean
    GroupedOpenApi authApis() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    GroupedOpenApi orderApis() {
        return GroupedOpenApi.builder()
                .group("orders")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    // ✅ 新增 UPMS 分組：兩種擇一（paths 或 package）
    @Bean
    GroupedOpenApi upmsApis() {
        return GroupedOpenApi.builder()
                .group("upms")
                .pathsToMatch("/api/upms/**")                // 用路徑分組
                // .packagesToScan("com.xk.truck.upms.controller.api") // 或用封包分組
                .build();
    }

    // ✅ 只在 dev/local 顯示 debug 分組
    @Bean
//    @Profile({"dev","local"})
    GroupedOpenApi debugApis() {
        return GroupedOpenApi.builder().group("debug").pathsToMatch("/_me").build();
    }
}
