package org.example.assignment2.controller;

import org.example.assignment2.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private CourseService courseService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredCourses", courseService.getFeaturedCourses());
        return "home";
    }
}