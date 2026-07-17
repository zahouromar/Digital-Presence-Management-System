package com.dpms.service;
import com.dpms.dto.StudentRequest;
import com.dpms.entity.Student;
import com.dpms.entity.User;
import com.dpms.exception.BadRequestException;
import com.dpms.exception.ResourceNotFoundException;
import com.dpms.repository.StudentRepository;
import com.dpms.repository.AttendanceRepository;
import com.dpms.repository.UserRepository;
import com.dpms.util.QrCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final String qrCodeDir;
    public StudentService(StudentRepository studentRepository, 
                          AttendanceRepository attendanceRepository,
                          UserRepository userRepository,
                          @Value("${storage.qrcode-dir}") String qrCodeDir) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.qrCodeDir = qrCodeDir;
    }
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }
    @Transactional
    public Student createStudent(StudentRequest request) {
        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().trim().isEmpty()) {
            throw new BadRequestException("Registration number is required");
        }
        if (studentRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new BadRequestException("Registration number already exists");
        }
        Student student = new Student(
                request.getRegistrationNumber(),
                request.getFirstName(),
                request.getLastName(),
                request.getGender(),
                request.getParentName(),
                request.getParentPhone()
        );
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder secretKeyBuilder = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        while (secretKeyBuilder.length() < 6) { 
            int index = (int) (rnd.nextFloat() * chars.length());
            secretKeyBuilder.append(chars.charAt(index));
        }
        student.setParentSecretKey(secretKeyBuilder.toString());
        Student savedStudent = studentRepository.save(student);
        generateAndSaveQrCode(savedStudent);
        User studentUser = new User();
        studentUser.setUsername(savedStudent.getRegistrationNumber());
        studentUser.setPassword(savedStudent.getRegistrationNumber()); 
        studentUser.setRole(com.dpms.entity.Role.STUDENT);
        studentUser.setFullName(savedStudent.getFirstName() + " " + savedStudent.getLastName());
        studentUser.setStudentId(savedStudent.getId());
        userRepository.save(studentUser);
        return savedStudent;
    }
    @Transactional
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().trim().isEmpty()) {
            Optional<Student> existing = studentRepository.findByRegistrationNumber(request.getRegistrationNumber());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new BadRequestException("Registration number already exists");
            }
            student.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getParentName() != null) {
            student.setParentName(request.getParentName());
        }
        if (request.getParentPhone() != null) {
            student.setParentPhone(request.getParentPhone());
        }
        return studentRepository.save(student);
    }
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        if (student.getQrCode() != null) {
            try {
                Path filePath = Paths.get(qrCodeDir, student.getQrCode());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Warning: Failed to delete QR code file: " + e.getMessage());
            }
        }
        attendanceRepository.deleteByStudent(student);
        userRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }
    public byte[] getQrCodeImageBytes(Long id) {
        Student student = getStudentById(id);
        if (student.getQrCode() == null) {
            throw new ResourceNotFoundException("QR Code not generated for student ID: " + id);
        }
        try {
            Path filePath = Paths.get(qrCodeDir, student.getQrCode());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResourceNotFoundException("QR Code image file not found on disk");
        }
    }
    private void generateAndSaveQrCode(Student student) {
        try {
            String qrContent = String.valueOf(student.getId());
            String fileName = "student_" + student.getId() + ".png";
            String filePath = Paths.get(qrCodeDir, fileName).toString();
            QrCodeGenerator.generateQrCodeImage(qrContent, 250, 250, filePath);
            student.setQrCode(fileName);
            studentRepository.save(student);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code for student ID: " + student.getId(), e);
        }
    }
    @Transactional
    public String importStudentsFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded CSV file is empty");
        }
        int importedCount = 0;
        int skippedCount = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length < 4) {
                    continue; 
                }
                String registrationNumber = cleanCsvField(fields[0]);
                String firstName = cleanCsvField(fields[1]);
                String lastName = cleanCsvField(fields[2]);
                String gender = cleanCsvField(fields[3]);
                String parentName = fields.length > 4 ? cleanCsvField(fields[4]) : "";
                String parentPhone = fields.length > 5 ? cleanCsvField(fields[5]) : "";
                if (registrationNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    continue; 
                }
                if (studentRepository.existsByRegistrationNumber(registrationNumber)) {
                    skippedCount++;
                    continue;
                }
                Student student = new Student(
                        registrationNumber,
                        firstName,
                        lastName,
                        gender,
                        parentName,
                        parentPhone
                );
                Student savedStudent = studentRepository.save(student);
                generateAndSaveQrCode(savedStudent);
                importedCount++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
        return "Successfully imported " + importedCount + " students. Skipped " + skippedCount + " duplicates.";
    }
    private String cleanCsvField(String field) {
        if (field == null) return "";
        String trimmed = field.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}