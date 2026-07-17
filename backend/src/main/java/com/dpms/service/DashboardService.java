package com.dpms.service;
import com.dpms.dto.DashboardResponse;
import com.dpms.entity.Role;
import com.dpms.repository.AttendanceRepository;
import com.dpms.repository.StudentRepository;
import com.dpms.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@Service
public class DashboardService {
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    public DashboardService(StudentRepository studentRepository,
                            AttendanceRepository attendanceRepository,
                            UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }
    public DashboardResponse getDashboardData() {
        long totalStudents = studentRepository.count();
        long todayAttendance = attendanceRepository.countByAttendanceDate(LocalDate.now());
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        List<String> trendDates = new ArrayList<>();
        List<Long> trendCounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            trendDates.add(date.format(formatter));
            trendCounts.add(attendanceRepository.countByAttendanceDate(date));
        }
        return new DashboardResponse(totalStudents, todayAttendance, totalTeachers, trendDates, trendCounts);
    }
}