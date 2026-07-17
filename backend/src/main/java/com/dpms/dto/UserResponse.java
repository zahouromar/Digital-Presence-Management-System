package com.dpms.dto;
import com.dpms.entity.Role;
import com.dpms.entity.User;
import java.time.LocalDateTime;
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private String fullName;
    private String phone;
    private Long studentId;
    private LocalDateTime createdAt;
    public UserResponse() {
    }
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.studentId = user.getStudentId();
        this.createdAt = user.getCreatedAt();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}