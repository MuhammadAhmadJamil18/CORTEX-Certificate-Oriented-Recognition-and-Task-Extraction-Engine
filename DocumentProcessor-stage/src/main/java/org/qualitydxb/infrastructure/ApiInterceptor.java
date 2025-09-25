package org.qualitydxb.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.qualitydxb.common.Enums.UserRole;
import org.qualitydxb.users.JwtUtility;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Handle preflight CORS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false; // Short-circuit further processing
        }

        String token = request.getHeader("token");
        String requestId = request.getHeader("request-id");
        String signature = request.getHeader("signature");

        if (token == null) {
            jsonErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Required header 'token' is missing.");
            return false;
        }

        JwtUtility jwtUtility = ApplicationContext.getBean(JwtUtility.class);

        if (jwtUtility.isTokenExpired(token)) {
            jsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired.");
            return false;
        }

        request.setAttribute("clientId", jwtUtility.extractClientId(token));
        request.setAttribute("userId", jwtUtility.extractUserId(token));
        Integer userRole= jwtUtility.extractUserRole(token);
        request.setAttribute("userRole", userRole);

        if (!checkAccess(userRole, request.getRequestURI())) {
            jsonErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return false;
        }

        setCorsHeaders(response);
        return true;
    }

    private boolean checkAccess(Integer userRole, String requestUri) {
        if ((requestUri.contains("/api/users/all") || requestUri.contains("/api/users/update") || requestUri.contains("/api/users/add") || requestUri.contains("/api/notification/notify") || requestUri.contains("/api/notification/schedule") ||  requestUri.contains("/api/notification/all")) && !userRole.equals(UserRole.ADMIN.getRole())) {
            return false;
        }
        return true;  // Default: Allow access to other routes
    }


    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", SystemProperties.getFrontendUrl());
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "token, request-id, signature, Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // No action needed here for CORS
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // No action needed here for CORS
    }

    private void jsonErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", String.valueOf(statusCode));
        errorResponse.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}
