package com.dpms.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @ManyToMany
    @JoinTable(
        name = "student_school_classes",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("students")
    private Set<SchoolClass> schoolClasses = new HashSet<>();
    @Column(nullable = false)
    private String gender;
    @Column(name = "parent_name")
    private String parentName;
    @Column(name = "parent_phone")
    private String parentPhone;
    @Column(name = "qr_code")
    private String qrCode;
    @Column(name = "parent_secret_key", nullable = false)
    private String parentSecretKey;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    public Student() {
    }
    public Student(String registrationNumber, String firstName, String lastName, String gender, String parentName, String parentPhone) {
        this.registrationNumber = registrationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.createdAt = LocalDateTime.now();
    }
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Set<SchoolClass> getSchoolClasses() {
        return schoolClasses;
    }
    public void setSchoolClasses(Set<SchoolClass> schoolClasses) {
        this.schoolClasses = schoolClasses;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getParentName() {
        return parentName;
    }
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    public String getParentPhone() {
        return parentPhone;
    }
    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
    public String getQrCode() {
        return qrCode;
    }
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getParentSecretKey() {
        return parentSecretKey;
    }
    public void setParentSecretKey(String parentSecretKey) {
        this.parentSecretKey = parentSecretKey;
    }
}