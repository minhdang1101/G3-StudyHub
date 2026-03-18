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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CommentRepository commentRepo;

    @Transactional(readOnly = true)
    @GetMapping("/admin/posts")
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

    @GetMapping("/admin/posts/new")
    public String showNewPostForm(Model model) {
        model.addAttribute("postDto", new PostDTO());
        model.addAttribute("authors", userRepo.findAll());
        return "post/post-submit";
    }

    @GetMapping("/admin/posts/edit/{id}")
    public String showEditPostForm(@PathVariable Integer id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) return "redirect:/admin/posts";

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setThumbnailUrl(post.getThumbnailUrl());
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
        }

        model.addAttribute("postDto", dto);
        model.addAttribute("authors", userRepo.findAll());
        return "post/post-submit";
    }

    @PostMapping("/admin/posts/save")
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
        post.setThumbnailUrl(postDto.getThumbnailUrl());
        
        if (postDto.getAuthorId() != null && postDto.getAuthorId() != 0) {
            User author = userRepo.findById(postDto.getAuthorId()).orElse(null);
            post.setAuthor(author);
        }

        postService.savePost(post);
        redirectAttributes.addFlashAttribute("message", "Saved post successfully!");
        return "redirect:/admin/posts";
    }

    @Transactional(readOnly = true)
    @GetMapping("/admin/posts/view/{id}")
    public String viewPost(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Post post = postService.getPostById(id);
        if (post == null) return "redirect:/admin/posts";

        model.addAttribute("post", post);

        // Fetch paginated comments
        Pageable pageable = PageRequest.of(page, 5, Sort.by("createdAt").descending());
        Page<Comment> commentsPage = commentRepo.findByPost_Id(id, pageable);
        model.addAttribute("commentsPage", commentsPage);
        
        // Prepare Comment Form
        CommentDTO commentDto = new CommentDTO();
        commentDto.setPostId(post.getId());
        model.addAttribute("commentDto", commentDto);
        
        model.addAttribute("users", userRepo.findAll());
        return "post/post-detail";
    }

    @GetMapping("/admin/posts/delete/{id}")
    public String deletePost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        postService.deletePost(id);
        redirectAttributes.addFlashAttribute("message", "Deleted post successfully!");
        return "redirect:/admin/posts";
    }

    // Public Blog endpoints
    @GetMapping("/blog")
    public String blogList(Model model,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer authorId) {
        // Only show PUBLISHED posts for public blog
        List<Post> posts = postService.getAllPosts("PUBLISHED", authorId, keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("authors", userRepo.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorId", authorId);
        return "blog/blog-list";
    }

    @GetMapping("/blog/{id}")
    public String blogDetail(@PathVariable Integer id, Model model, HttpSession session) {
        Post post = postService.getPostById(id);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            return "redirect:/blog";
        }

        model.addAttribute("post", post);
        
        // Get current user from session for comment
        User currentUser = (User) session.getAttribute("user");
        model.addAttribute("currentUser", currentUser);
        
        // Prepare Comment Form
        CommentDTO commentDto = new CommentDTO();
        commentDto.setPostId(post.getId());
        if (currentUser != null) {
            commentDto.setUserId(currentUser.getId());
        }
        model.addAttribute("commentDto", commentDto);
        
        return "blog/blog-detail";
    }

    @PostMapping("/blog/comment/add")
    public String addBlogComment(@Valid @ModelAttribute("commentDto") CommentDTO commentDto,
                             BindingResult result,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to comment!");
            return "redirect:/blog/" + commentDto.getPostId();
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Comment content is required!");
            return "redirect:/blog/" + commentDto.getPostId();
        }

        Comment comment = new Comment();
        comment.setComment(commentDto.getComment());
        
        Post post = postService.getPostById(commentDto.getPostId());
        comment.setPost(post);
        comment.setUser(currentUser);

        commentRepo.save(comment);
        redirectAttributes.addFlashAttribute("message", "Comment added successfully!");
        return "redirect:/blog/" + commentDto.getPostId();
    }

    @PostMapping("/admin/posts/comment/add")
    public String addComment(@Valid @ModelAttribute("commentDto") CommentDTO commentDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
             Post post = postService.getPostById(commentDto.getPostId());
             if (post == null) return "redirect:/admin/posts";
             
             model.addAttribute("post", post);
             model.addAttribute("users", userRepo.findAll());
             return "post/post-detail";
        }

        Comment comment = new Comment();
        comment.setComment(commentDto.getComment());
        
        Post post = postService.getPostById(commentDto.getPostId());
        comment.setPost(post);

        if (commentDto.getUserId() != null && commentDto.getUserId() != 0) {
            User user = userRepo.findById(commentDto.getUserId()).orElse(null);
            comment.setUser(user);
        }

        commentRepo.save(comment);
        redirectAttributes.addFlashAttribute("message", "Added comment successfully!");
        return "redirect:/admin/posts/view/" + commentDto.getPostId();
    }
}