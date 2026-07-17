package com.dpms.repository;
import com.dpms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByAttendanceDate(LocalDate date);
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    boolean existsByStudentIdAndAttendanceDate(Long studentId, LocalDate date);
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);
    long countByAttendanceDate(LocalDate date);
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.student = :student")
    void deleteByStudent(@Param("student") com.dpms.entity.Student student);
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.schoolClass = :schoolClass")
    void deleteBySchoolClass(@Param("schoolClass") com.dpms.entity.SchoolClass schoolClass);
}