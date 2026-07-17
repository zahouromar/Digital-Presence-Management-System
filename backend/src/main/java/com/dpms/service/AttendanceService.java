package com.dpms.service;
import com.dpms.dto.AttendanceResponse;
import com.dpms.dto.ManualAttendanceRequest;
import com.dpms.dto.ScanRequest;
import com.dpms.dto.ScanResponse;
import com.dpms.entity.Attendance;
import com.dpms.entity.AttendanceStatus;
import com.dpms.entity.Student;
import com.dpms.exception.BadRequestException;
import com.dpms.exception.ResourceNotFoundException;
import com.dpms.repository.AttendanceRepository;
import com.dpms.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final com.dpms.repository.SchoolClassRepository schoolClassRepository;
    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository, com.dpms.repository.SchoolClassRepository schoolClassRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
    }
    @Transactional
    public ScanResponse scanQr(ScanRequest request, Long classId) {
        if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
            throw new BadRequestException("Registration Number is missing from scan");
        }
        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getRegistrationNumber().equals(request.getStudentId().trim()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with Registration Number: " + request.getStudentId()));
        com.dpms.entity.SchoolClass schoolClass = null;
        if (classId != null) {
            schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
            boolean inClass = student.getSchoolClasses().stream().anyMatch(c -> c.getId().equals(classId));
            if (!inClass) {
                throw new BadRequestException("Student does not belong to this class");
            }
        }
        LocalDate today = LocalDate.now();
        final com.dpms.entity.SchoolClass finalSchoolClass = schoolClass;
        if (finalSchoolClass != null && attendanceRepository.findAll().stream().anyMatch(a -> a.getStudent().getId().equals(student.getId()) && a.getAttendanceDate().equals(today) && a.getSchoolClass().getId().equals(finalSchoolClass.getId()))) {
            throw new BadRequestException("Attendance Already Recorded");
        }
        LocalTime now = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        AttendanceStatus status = AttendanceStatus.PRESENT;
        Attendance attendance = new Attendance(
                student,
                schoolClass,
                today,
                now,
                status
        );
        attendanceRepository.save(attendance);
        String fullName = student.getFirstName() + " " + student.getLastName();
        String timeString = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return new ScanResponse(
                "Attendance Recorded",
                fullName,
                timeString
        );
    }
    public List<AttendanceResponse> getAllAttendance(LocalDate date, Long classId) {
        List<Attendance> records;
        if (date == null) {
            records = attendanceRepository.findAll();
        } else {
            records = attendanceRepository.findByAttendanceDate(date);
        }
        return records.stream()
                .filter(a -> classId == null || (a.getSchoolClass() != null && a.getSchoolClass().getId().equals(classId)))
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
    }
    public List<AttendanceResponse> getClassAttendanceForDate(Long classId, LocalDate date) {
        com.dpms.entity.SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        List<Attendance> existingRecords = attendanceRepository.findByAttendanceDate(date).stream()
                .filter(a -> a.getSchoolClass() != null && a.getSchoolClass().getId().equals(classId))
                .collect(Collectors.toList());
        List<AttendanceResponse> responses = new java.util.ArrayList<>();
        for (Student student : schoolClass.getStudents()) {
            Optional<Attendance> attendance = existingRecords.stream()
                    .filter(a -> a.getStudent().getId().equals(student.getId()))
                    .findFirst();
            if (attendance.isPresent()) {
                responses.add(new AttendanceResponse(attendance.get()));
            } else {
                AttendanceResponse missing = new AttendanceResponse();
                missing.setStudentName(student.getFirstName() + " " + student.getLastName());
                missing.setRegistrationNumber(student.getRegistrationNumber());
                missing.setClassName(schoolClass.getName());
                missing.setClassId(classId);
                missing.setAttendanceDate(date);
                missing.setStatus(AttendanceStatus.NOT_MARKED.name());
                responses.add(missing);
            }
        }
        return responses;
    }
    @Transactional
    public void markRemainingAsAbsent(Long classId, LocalDate date) {
        com.dpms.entity.SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        List<Attendance> existingRecords = attendanceRepository.findByAttendanceDate(date).stream()
                .filter(a -> a.getSchoolClass() != null && a.getSchoolClass().getId().equals(classId))
                .collect(Collectors.toList());
        for (Student student : schoolClass.getStudents()) {
            boolean hasRecord = existingRecords.stream()
                    .anyMatch(a -> a.getStudent().getId().equals(student.getId()));
            if (!hasRecord) {
                Attendance attendance = new Attendance(
                        student,
                        schoolClass,
                        date,
                        LocalTime.of(0, 0),
                        AttendanceStatus.ABSENT
                );
                attendanceRepository.save(attendance);
            }
        }
    }
    public List<AttendanceResponse> getTodayAttendance(Long classId) {
        return getAllAttendance(LocalDate.now(), classId);
    }
    public List<AttendanceResponse> getAttendanceByStudentId(Long studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId)
                .stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public AttendanceResponse saveManualAttendance(ManualAttendanceRequest request, Long classId) {
        if (request.getStudentId() == null) {
            throw new BadRequestException("Student ID is required");
        }
        if (request.getDate() == null) {
            throw new BadRequestException("Date is required");
        }
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new BadRequestException("Status is required");
        }
        AttendanceStatus status;
        try {
            status = AttendanceStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid attendance status: " + request.getStatus());
        }
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));
        com.dpms.entity.SchoolClass schoolClass = null;
        if (classId != null) {
            schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
            boolean inClass = student.getSchoolClasses().stream().anyMatch(c -> c.getId().equals(classId));
            if (!inClass) {
                throw new BadRequestException("Student does not belong to this class");
            }
        }
        final com.dpms.entity.SchoolClass finalSchoolClass = schoolClass;
        Optional<Attendance> existingRecord = attendanceRepository.findAll().stream()
                .filter(a -> a.getStudent().getId().equals(student.getId()) && a.getAttendanceDate().equals(request.getDate()) && (finalSchoolClass == null || (a.getSchoolClass() != null && a.getSchoolClass().getId().equals(finalSchoolClass.getId()))))
                .findFirst();
        Attendance attendance;
        LocalTime now = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        if (existingRecord.isPresent()) {
            attendance = existingRecord.get();
            attendance.setStatus(status);
            if (status == AttendanceStatus.ABSENT) {
                attendance.setCheckInTime(LocalTime.of(0, 0));
            } else {
                attendance.setCheckInTime(now);
            }
        } else {
            LocalTime checkIn = (status == AttendanceStatus.ABSENT) ? LocalTime.of(0, 0) : now;
            attendance = new Attendance(
                    student,
                    schoolClass,
                    request.getDate(),
                    checkIn,
                    status
            );
        }
        Attendance saved = attendanceRepository.save(attendance);
        return new AttendanceResponse(saved);
    }
}