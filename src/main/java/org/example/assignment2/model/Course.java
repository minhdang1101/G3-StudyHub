package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "course")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "price")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "level", referencedColumnName = "setting_id")
    private Setting level;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "setting_id")
    private Setting category;

    @ManyToOne
    @JoinColumn(name = "instructor_id", referencedColumnName = "user_id")
    private User instructor;

    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<Chapter> chapters;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}