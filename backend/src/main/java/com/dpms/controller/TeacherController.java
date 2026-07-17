package com.dpms.controller;
import com.dpms.dto.TeacherRequest;
import com.dpms.dto.UserResponse;
import com.dpms.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/teachers")
public class TeacherController {
    private final UserService userService;
    public TeacherController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        List<UserResponse> teachers = userService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }
    @PostMapping
    public ResponseEntity<UserResponse> createTeacher(@RequestBody TeacherRequest request) {
        UserResponse response = userService.createTeacher(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateTeacher(@PathVariable Long id, @RequestBody TeacherRequest request) {
        UserResponse response = userService.updateTeacher(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        userService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}