package com.dpms.dto;
import java.time.LocalDate;
public class ManualAttendanceRequest {
    private Long studentId;
    private Long classId;
    private LocalDate date;
    private String status;
    public ManualAttendanceRequest() {
    }
    public ManualAttendanceRequest(Long studentId, Long classId, LocalDate date, String status) {
        this.studentId = studentId;
        this.classId = classId;
        this.date = date;
        this.status = status;
    }
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getClassId() {
        return classId;
    }
    public void setClassId(Long classId) {
        this.classId = classId;
    }
}