package com.dpms.dto;
import com.dpms.entity.Attendance;
import java.time.LocalDate;
import java.time.LocalTime;
public class AttendanceResponse {
    private Long id;
    private String studentName;
    private String registrationNumber;
    private String className;
    private Long classId;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private String status;
    public AttendanceResponse() {
    }
    public AttendanceResponse(Attendance attendance) {
        this.id = attendance.getId();
        this.studentName = attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName();
        this.registrationNumber = attendance.getStudent().getRegistrationNumber();
        if (attendance.getSchoolClass() != null) {
            this.className = attendance.getSchoolClass().getName();
            this.classId = attendance.getSchoolClass().getId();
        }
        this.attendanceDate = attendance.getAttendanceDate();
        this.checkInTime = attendance.getCheckInTime();
        this.status = attendance.getStatus().name();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public Long getClassId() {
        return classId;
    }
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }
    public LocalTime getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}