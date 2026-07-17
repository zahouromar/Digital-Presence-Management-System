package com.dpms.dto;
import java.util.List;
public class DashboardResponse {
    private long totalStudents;
    private long todayAttendance;
    private long totalTeachers;
    private List<String> trendDates;
    private List<Long> trendCounts;
    public DashboardResponse() {
    }
    public DashboardResponse(long totalStudents, long todayAttendance, long totalTeachers, List<String> trendDates, List<Long> trendCounts) {
        this.totalStudents = totalStudents;
        this.todayAttendance = todayAttendance;
        this.totalTeachers = totalTeachers;
        this.trendDates = trendDates;
        this.trendCounts = trendCounts;
    }
    public long getTotalStudents() {
        return totalStudents;
    }
    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }
    public long getTodayAttendance() {
        return todayAttendance;
    }
    public void setTodayAttendance(long todayAttendance) {
        this.todayAttendance = todayAttendance;
    }
    public long getTotalTeachers() {
        return totalTeachers;
    }
    public void setTotalTeachers(long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }
    public List<String> getTrendDates() {
        return trendDates;
    }
    public void setTrendDates(List<String> trendDates) {
        this.trendDates = trendDates;
    }
    public List<Long> getTrendCounts() {
        return trendCounts;
    }
    public void setTrendCounts(List<Long> trendCounts) {
        this.trendCounts = trendCounts;
    }
}