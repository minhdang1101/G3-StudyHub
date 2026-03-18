package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "chapter")
@Data
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Integer chapterId;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "course_id")
    private Course course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons;
}