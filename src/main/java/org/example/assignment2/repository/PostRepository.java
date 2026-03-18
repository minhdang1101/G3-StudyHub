package org.example.assignment2.repository;

import org.example.assignment2.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    boolean existsByTitle(String title);

    @Query("SELECT p FROM Post p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:authorId IS NULL OR p.author.id = :authorId) AND " +
            "(:keyword IS NULL OR p.title LIKE %:keyword%)")
    List<Post> searchPosts(@Param("status") String status,
                           @Param("authorId") Integer authorId,
                           @Param("keyword") String keyword);

    long countByAuthorId(Integer authorId);

    List<Post> findByAuthorId(Integer authorId);
    List<Post> findTop4ByStatusOrderByCreatedAtDesc(String status);
}