package com.dpms.service;
import com.dpms.dto.UserResponse;
import com.dpms.entity.User;
import com.dpms.exception.BadRequestException;
import com.dpms.repository.UserRepository;
import com.dpms.repository.StudentRepository;
import com.dpms.entity.Student;
import com.dpms.dto.ParentRegistrationRequest;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    public AuthService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }
    public UserResponse login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Username and password are required");
        }
        if (userRepository.countByRole(com.dpms.entity.Role.ADMIN) == 0) {
            User newAdmin = new User();
            newAdmin.setUsername(username.trim());
            newAdmin.setPassword(password);
            newAdmin.setRole(com.dpms.entity.Role.ADMIN);
            newAdmin.setFullName("System Administrator");
            userRepository.save(newAdmin);
            return new UserResponse(newAdmin);
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        if (!user.getPassword().equals(password)) {
            throw new BadRequestException("Invalid username or password");
        }
        return new UserResponse(user);
    }
    public void registerParent(ParentRegistrationRequest request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getSecretKey() == null) {
            throw new BadRequestException("All fields are required");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username is not valid (already taken)");
        }
        Student student = studentRepository.findByRegistrationNumber(request.getRegistrationNumber())
                .orElseThrow(() -> new BadRequestException("Student not found"));
        if (!student.getParentSecretKey().equals(request.getSecretKey())) {
            throw new BadRequestException("Invalid secret key for this student");
        }
        User parentUser = new User();
        parentUser.setUsername(request.getUsername());
        parentUser.setPassword(request.getPassword());
        parentUser.setRole(com.dpms.entity.Role.PARENT);
        parentUser.setFullName(request.getFullName());
        parentUser.setStudentId(student.getId());
        userRepository.save(parentUser);
    }
}