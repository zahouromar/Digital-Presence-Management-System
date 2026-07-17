package com.dpms.controller;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Role;
import com.dpms.entity.SchoolClass;
import com.dpms.service.SchoolClassService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/classes")
public class SchoolClassController {
    private final SchoolClassService schoolClassService;
    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }
    @GetMapping
    public ResponseEntity<List<SchoolClass>> getAllClasses(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(schoolClassService.getAllClasses());
    }
    @GetMapping("/my-classes")
    public ResponseEntity<List<SchoolClass>> getMyClasses(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.TEACHER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(schoolClassService.getClassesByTeacher(user.getId()));
    }
    @PostMapping
    public ResponseEntity<SchoolClass> createClass(@RequestBody Map<String, String> request, HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String name = request.get("name");
        Long teacherId = request.containsKey("teacherId") ? Long.parseLong(request.get("teacherId")) : null;
        return ResponseEntity.ok(schoolClassService.createClass(name, teacherId));
    }
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> assignStudentToClass(@PathVariable Long classId, @PathVariable Long studentId, HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        schoolClassService.assignStudentToClass(classId, studentId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> removeStudentFromClass(@PathVariable Long classId, @PathVariable Long studentId, HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        schoolClassService.removeStudentFromClass(classId, studentId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId, HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        schoolClassService.deleteClass(classId);
        return ResponseEntity.ok().build();
    }
}