package com.lowcarbon.platform.security;

import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AuthInterceptor(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login")
                || path.startsWith("/h2-console")
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException("\u672a\u767b\u5f55\u6216\u4ee4\u724c\u65e0\u6548");
        }

        String token = authHeader.substring(7).trim();
        Long userId = tokenService.resolveUserId(token);
        if (userId == null) {
            throw new ApiException("\u767b\u5f55\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("\u7528\u6237\u4e0d\u5b58\u5728"));
        AuthContext.set(user);

        if (path.startsWith("/api/admin") && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("\u65e0\u7ba1\u7406\u5458\u6743\u9650");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
