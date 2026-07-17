package com.dpms.controller;
import com.dpms.dto.StudentRequest;
import com.dpms.entity.Student;
import com.dpms.service.StudentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody StudentRequest request) {
        Student student = studentService.createStudent(request);
        return new ResponseEntity<>(student, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody StudentRequest request) {
        Student student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(student);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getStudentQrCode(@PathVariable Long id) {
        byte[] imageBytes = studentService.getQrCodeImageBytes(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "qrcode_student_" + id + ".png");
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    @PostMapping("/import")
    public ResponseEntity<java.util.Map<String, String>> importStudents(@RequestParam("file") MultipartFile file) {
        String result = studentService.importStudentsFromCsv(file);
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", result));
    }
}