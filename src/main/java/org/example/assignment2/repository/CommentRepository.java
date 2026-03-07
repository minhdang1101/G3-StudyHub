package org.example.assignment2.repository;

import org.example.assignment2.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    long countByUserId(Integer userId);
    List<Comment> findByUserId(Integer userId);
}