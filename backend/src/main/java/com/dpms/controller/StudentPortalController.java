package com.dpms.controller;
import com.dpms.dto.AttendanceResponse;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Student;
import com.dpms.service.AttendanceService;
import com.dpms.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/my")
public class StudentPortalController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    public StudentPortalController(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }
    @GetMapping("/profile")
    public ResponseEntity<Student> getMyProfile(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getStudentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Student student = studentService.getStudentById(user.getStudentId());
        return ResponseEntity.ok(student);
    }
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getStudentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AttendanceResponse> records = attendanceService.getAttendanceByStudentId(user.getStudentId());
        return ResponseEntity.ok(records);
    }
    @GetMapping("/qrcode")
    public ResponseEntity<byte[]> getMyQrCode(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null || user.getStudentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] imageBytes = studentService.getQrCodeImageBytes(user.getStudentId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}