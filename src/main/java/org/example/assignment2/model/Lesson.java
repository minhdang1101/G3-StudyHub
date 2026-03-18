package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lesson")
@Data
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Integer lessonId;

    @ManyToOne
    @JoinColumn(name = "chapter_id", referencedColumnName = "chapter_id")
    private Chapter chapter;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "content_url", columnDefinition = "TEXT")
    private String contentUrl;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_preview")
    private Boolean isPreview;

    @Column(name = "status")
    private Integer status;
}