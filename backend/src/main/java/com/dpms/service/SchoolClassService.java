package com.dpms.service;
import com.dpms.entity.SchoolClass;
import com.dpms.entity.Student;
import com.dpms.entity.User;
import com.dpms.exception.ResourceNotFoundException;
import com.dpms.repository.SchoolClassRepository;
import com.dpms.repository.StudentRepository;
import com.dpms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class SchoolClassService {
    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final com.dpms.repository.AttendanceRepository attendanceRepository;
    public SchoolClassService(SchoolClassRepository schoolClassRepository, UserRepository userRepository, StudentRepository studentRepository, com.dpms.repository.AttendanceRepository attendanceRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }
    public List<SchoolClass> getAllClasses() {
        return schoolClassRepository.findAll();
    }
    public List<SchoolClass> getClassesByTeacher(Long teacherId) {
        return schoolClassRepository.findByTeacherId(teacherId);
    }
    public SchoolClass getClassById(Long classId) {
        return schoolClassRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
    }
    @Transactional
    public SchoolClass createClass(String name, Long teacherId) {
        User teacher = null;
        if (teacherId != null) {
            teacher = userRepository.findById(teacherId).orElse(null);
        }
        SchoolClass sc = new SchoolClass(name, teacher);
        return schoolClassRepository.save(sc);
    }
    @Transactional
    public void assignStudentToClass(Long classId, Long studentId) {
        SchoolClass sc = getClassById(classId);
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!sc.getStudents().contains(student)) {
            sc.getStudents().add(student);
            student.getSchoolClasses().add(sc);
            schoolClassRepository.save(sc);
        }
    }
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        SchoolClass sc = getClassById(classId);
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        sc.getStudents().remove(student);
        student.getSchoolClasses().remove(sc);
        schoolClassRepository.save(sc);
        studentRepository.save(student);
    }
    @Transactional
    public void deleteClass(Long classId) {
        SchoolClass sc = getClassById(classId);
        attendanceRepository.deleteBySchoolClass(sc);
        for (Student s : sc.getStudents()) {
            s.getSchoolClasses().remove(sc);
            studentRepository.save(s);
        }
        sc.getStudents().clear();
        schoolClassRepository.delete(sc);
    }
}