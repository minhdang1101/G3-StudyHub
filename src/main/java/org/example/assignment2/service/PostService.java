package org.example.assignment2.service;

import org.example.assignment2.model.Post;
import org.example.assignment2.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepo;

    public List<Post> getAllPosts(String status, Integer authorId, String keyword) {
        return postRepo.searchPosts(status, authorId, keyword);
    }

    public Post getPostById(Integer id) {
        return postRepo.findById(id).orElse(null);
    }

    public void savePost(Post post) {
        postRepo.save(post);
    }

    public void deletePost(Integer id) {
        postRepo.deleteById(id);
    }
}