package com.xk.truck.web;

import com.xk.base.web.ApiResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.xk.truck.api") // ✅ 只攔截 truck 的 API
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果 Controller 已經回傳 ApiResult，就不要再包一次
        return !returnType.getParameterType().equals(ApiResult.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // null → 空成功
        if (body == null) {
            return ApiResult.success(null, "OK");
        }

        // 已經是 ApiResult → 原樣回傳
        if (body instanceof ApiResult) {
            return body;
        }

        // 其他 → 包裝成 ApiResult
        return ApiResult.success(body, "OK");
    }
}
