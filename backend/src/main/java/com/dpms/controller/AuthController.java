package com.dpms.controller;
import com.dpms.dto.LoginRequest;
import com.dpms.dto.UserResponse;
import com.dpms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        UserResponse userResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        HttpSession session = request.getSession(true);
        session.setAttribute("user", userResponse);
        return ResponseEntity.ok(userResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register-parent")
    public ResponseEntity<Map<String, String>> registerParent(@RequestBody com.dpms.dto.ParentRegistrationRequest request) {
        authService.registerParent(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Parent account created successfully");
        return ResponseEntity.ok(response);
    }
}