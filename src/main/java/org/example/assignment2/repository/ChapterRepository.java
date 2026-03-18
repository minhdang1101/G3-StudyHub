package org.example.assignment2.repository;

import org.example.assignment2.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

    List<Chapter> findByCourseCourseIdOrderByOrderIndexAsc(Long courseId);
}