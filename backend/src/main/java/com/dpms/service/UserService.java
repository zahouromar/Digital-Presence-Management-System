package com.dpms.service;
import com.dpms.dto.TeacherRequest;
import com.dpms.dto.UserResponse;
import com.dpms.entity.Role;
import com.dpms.entity.User;
import com.dpms.exception.BadRequestException;
import com.dpms.exception.ResourceNotFoundException;
import com.dpms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserResponse> getAllTeachers() {
        return userRepository.findByRole(Role.TEACHER).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public UserResponse createTeacher(TeacherRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Username is already taken");
        }
        User teacher = new User(
                request.getUsername(),
                request.getPassword(),
                Role.TEACHER,
                request.getFullName(),
                request.getPhone()
        );
        User saved = userRepository.save(teacher);
        return new UserResponse(saved);
    }
    @Transactional
    public UserResponse updateTeacher(Long id, TeacherRequest request) {
        User teacher = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        if (teacher.getRole() != Role.TEACHER) {
            throw new BadRequestException("User is not a teacher");
        }
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new BadRequestException("Username is already taken");
            }
            teacher.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            teacher.setPassword(request.getPassword());
        }
        if (request.getFullName() != null) {
            teacher.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }
        User updated = userRepository.save(teacher);
        return new UserResponse(updated);
    }
    @Transactional
    public void deleteTeacher(Long id) {
        User teacher = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        if (teacher.getRole() != Role.TEACHER) {
            throw new BadRequestException("User is not a teacher");
        }
        userRepository.delete(teacher);
    }
}