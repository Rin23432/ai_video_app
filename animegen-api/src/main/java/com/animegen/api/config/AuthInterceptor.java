package com.animegen.api.config;

import com.animegen.common.AuthContext;
import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Value("${animegen.jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "login required");
        }
        String userId = JwtUtils.verifyAndGetUserId(auth.substring(7), jwtSecret);
        if (userId == null) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "token invalid or expired");
        }
        AuthContext.setUserId(Long.parseLong(userId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
