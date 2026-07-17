package com.dpms.repository;
import com.dpms.entity.Role;
import com.dpms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    long countByRole(Role role);
    @Modifying
    @Query("DELETE FROM User u WHERE u.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}