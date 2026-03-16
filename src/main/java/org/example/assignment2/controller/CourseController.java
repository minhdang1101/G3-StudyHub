package org.example.assignment2.controller;

import org.example.assignment2.model.Course;
import org.example.assignment2.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/courses")
    public String publicCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Model model
    ) {

        List<Course> courses;

        // search + filter
        if (keyword != null && !keyword.isEmpty()) {
            courses = courseRepository.findByTitleContainingIgnoreCase(keyword);

        } else if (categoryId != null) {
            courses = courseRepository.findByCategoryId(categoryId);

        } else {
            courses = courseRepository.findAll();
        }

        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);

        return "course/public-courses";
    }

}
