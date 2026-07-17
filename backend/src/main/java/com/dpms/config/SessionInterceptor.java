package com.dpms.config;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.equals("/login") || path.equals("/logout") || path.equals("/error") || path.equals("/register-parent")) {
            return true;
        }
        if (path.matches("^/students/\\d+/qrcode$") && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Unauthorized: Please log in.\"}");
            return false;
        }
        UserResponse user = (UserResponse) session.getAttribute("user");
        boolean isAdminEndpoint = path.startsWith("/teachers") ||
                                  path.startsWith("/students") ||
                                  path.startsWith("/dashboard") ||
                                  (path.equals("/attendance") && "GET".equalsIgnoreCase(request.getMethod()));
        if (isAdminEndpoint && user.getRole() != Role.ADMIN) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Forbidden: Admin access required.\"}");
            return false;
        }
        boolean isStudentPortalEndpoint = path.startsWith("/my");
        if (isStudentPortalEndpoint && user.getRole() != Role.STUDENT) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Forbidden: Student access required.\"}");
            return false;
        }
        boolean isParentPortalEndpoint = path.startsWith("/parent");
        if (isParentPortalEndpoint && user.getRole() != Role.PARENT) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Forbidden: Parent access required.\"}");
            return false;
        }
        return true;
    }
}