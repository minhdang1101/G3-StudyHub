package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
@Data
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "enroll_reason", columnDefinition = "TEXT")
    private String enrollReason;

    @Column(name = "status")
    private String status;

    @Column(name = "reject_notes", columnDefinition = "TEXT")
    private String rejectNotes;

    @Column(name = "progress")
    private Double progress;

    @Column(name = "enrolled_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime enrolledAt;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}