package com.dpms.entity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "school_classes")
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
    @ManyToMany(mappedBy = "schoolClasses")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("schoolClasses")
    private Set<Student> students = new HashSet<>();
    public SchoolClass() {}
    public SchoolClass(String name, User teacher) {
        this.name = name;
        this.teacher = teacher;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public Set<Student> getStudents() { return students; }
    public void setStudents(Set<Student> students) { this.students = students; }
}