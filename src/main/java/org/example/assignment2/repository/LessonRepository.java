package org.example.assignment2.repository;

import org.example.assignment2.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByChapterChapterIdOrderByOrderIndexAsc(Integer chapterId);
}