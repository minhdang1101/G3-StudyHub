package org.example.assignment2.controller;

import jakarta.validation.Valid;
import org.example.assignment2.dto.CommentDTO;
import org.example.assignment2.dto.PostDTO;
import org.example.assignment2.model.Post;
import org.example.assignment2.model.User;
import org.example.assignment2.model.Comment;
import org.example.assignment2.service.PostService;
import org.example.assignment2.repository.UserRepository;
import org.example.assignment2.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CommentRepository commentRepo;

    @GetMapping
    public String listPosts(Model model,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) Integer authorId,
                            @RequestParam(required = false) String keyword) {

        List<Post> posts = postService.getAllPosts(status, authorId, keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("authors", userRepo.findAll());

        model.addAttribute("status", status);
        model.addAttribute("authorId", authorId);
        model.addAttribute("keyword", keyword);

        return "post/post-list";
    }

    @GetMapping("/new")
    public String showNewPostForm(Model model) {
        model.addAttribute("postDto", new PostDTO());
        model.addAttribute("authors", userRepo.findAll());
        return "post/post-submit";
    }

    @GetMapping("/edit/{id}")
    public String showEditPostForm(@PathVariable Integer id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) return "redirect:/posts";

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
        }

        model.addAttribute("postDto", dto);
        model.addAttribute("authors", userRepo.findAll());
        return "post/post-submit";
    }

    @PostMapping("/save")
    public String savePost(@Valid @ModelAttribute("postDto") PostDTO postDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("authors", userRepo.findAll());
            return "post/post-submit";
        }

        Post post;
        if (postDto.getId() != null) {
            post = postService.getPostById(postDto.getId());
            if (post == null) post = new Post();
        } else {
            post = new Post();
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setStatus(postDto.getStatus());
        
        if (postDto.getAuthorId() != null) {
            User author = userRepo.findById(postDto.getAuthorId()).orElse(null);
            post.setAuthor(author);
        }

        postService.savePost(post);
        redirectAttributes.addFlashAttribute("message", "Saved post successfully!");
        return "redirect:/posts";
    }

    @GetMapping("/view/{id}")
    public String viewPost(@PathVariable Integer id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) return "redirect:/posts";

        model.addAttribute("post", post);
        
        // Prepare Comment Form
        CommentDTO commentDto = new CommentDTO();
        commentDto.setPostId(post.getId());
        model.addAttribute("commentDto", commentDto);
        
        model.addAttribute("users", userRepo.findAll());
        return "post/post-detail";
    }

    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        postService.deletePost(id);
        redirectAttributes.addFlashAttribute("message", "Deleted post successfully!");
        return "redirect:/posts";
    }

    // 7. Add Comment via Detail Page
    @PostMapping("/comment/add")
    public String addComment(@Valid @ModelAttribute("commentDto") CommentDTO commentDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
             Post post = postService.getPostById(commentDto.getPostId());
             if (post == null) return "redirect:/posts";
             
             model.addAttribute("post", post);
             model.addAttribute("users", userRepo.findAll());
             return "post/post-detail";
        }

        Comment comment = new Comment();
        comment.setComment(commentDto.getComment());
        
        Post post = postService.getPostById(commentDto.getPostId());
        comment.setPost(post);

        if (commentDto.getUserId() != null) {
            User user = userRepo.findById(commentDto.getUserId()).orElse(null);
            comment.setUser(user);
        }

        commentRepo.save(comment);
        redirectAttributes.addFlashAttribute("message", "Added comment successfully!");
        return "redirect:/posts/view/" + commentDto.getPostId();
    }
}