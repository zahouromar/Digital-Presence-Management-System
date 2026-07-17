package com.dpms.controller;
import com.dpms.dto.AttendanceResponse;
import com.dpms.dto.ScanRequest;
import com.dpms.dto.ScanResponse;
import com.dpms.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Role;
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scanQr(@RequestBody ScanRequest request, HttpSession session) {
        Long classId = request.getClassId();
        ScanResponse response = attendanceService.scanQr(request, classId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            HttpSession session) {
        List<AttendanceResponse> response = attendanceService.getAllAttendance(date, classId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceResponse>> getTodayAttendance(
            @RequestParam(required = false) Long classId,
            HttpSession session) {
        List<AttendanceResponse> response = attendanceService.getTodayAttendance(classId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<AttendanceResponse>> getClassAttendanceForDate(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session) {
        List<AttendanceResponse> response = attendanceService.getClassAttendanceForDate(classId, date);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/class/{classId}/mark-absent")
    public ResponseEntity<Void> markRemainingAsAbsent(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session) {
        attendanceService.markRemainingAsAbsent(classId, date);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/manual")
    public ResponseEntity<AttendanceResponse> saveManualAttendance(@RequestBody com.dpms.dto.ManualAttendanceRequest request, HttpSession session) {
        Long classId = request.getClassId();
        AttendanceResponse response = attendanceService.saveManualAttendance(request, classId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}