package com.dpms.controller;
import com.dpms.dto.AttendanceResponse;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Student;
import com.dpms.service.AttendanceService;
import com.dpms.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/parent")
public class ParentPortalController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    public ParentPortalController(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }
    @GetMapping("/child")
    public ResponseEntity<Student> getChildProfile(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getStudentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Student student = studentService.getStudentById(user.getStudentId());
        return ResponseEntity.ok(student);
    }
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponse>> getChildAttendance(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getStudentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AttendanceResponse> records = attendanceService.getAttendanceByStudentId(user.getStudentId());
        return ResponseEntity.ok(records);
    }
}