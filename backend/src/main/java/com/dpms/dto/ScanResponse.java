package com.dpms.dto;
public class ScanResponse {
    private String message;
    private String studentName;
    private String time;
    public ScanResponse() {
    }
    public ScanResponse(String message, String studentName, String time) {
        this.message = message;
        this.studentName = studentName;
        this.time = time;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}