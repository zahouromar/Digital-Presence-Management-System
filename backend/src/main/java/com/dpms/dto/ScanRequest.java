package com.dpms.dto;
public class ScanRequest {
    private String studentId;
    private Long classId;
    public ScanRequest() {
    }
    public ScanRequest(String studentId, Long classId) {
        this.studentId = studentId;
        this.classId = classId;
    }
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public Long getClassId() {
        return classId;
    }
    public void setClassId(Long classId) {
        this.classId = classId;
    }
}