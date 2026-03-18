package org.example.assignment2.controller;

import jakarta.validation.Valid;
import org.example.assignment2.dto.ChapterDTO;
import org.example.assignment2.dto.CourseDTO;
import org.example.assignment2.dto.LessonDTO;
import org.example.assignment2.model.Chapter;
import org.example.assignment2.model.Course;
import org.example.assignment2.model.Lesson;
import org.example.assignment2.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("")
    public String listCourses(Model model,
                              @RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "categoryId", required = false) Integer categoryId,
                              @RequestParam(name = "instructorId", required = false) Integer instructorId,
                              @RequestParam(name = "status", required = false) String status) {
        model.addAttribute("courses", courseService.getCourses(keyword, categoryId, instructorId, status));
        model.addAttribute("categoryList", courseService.getAllCategories());
        model.addAttribute("instructorList", courseService.getAllInstructors());

        model.addAttribute("keyword", keyword);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentInstructorId", instructorId);
        model.addAttribute("currentStatus", status);

        return "course/course-list";
    }

    @GetMapping("/new")
    public String showAddCourseForm(Model model) {
        CourseDTO courseDto = new CourseDTO();
        courseDto.setStatus("Published");

        model.addAttribute("courseDto", courseDto);
        loadCourseFormData(model);

        return "course/course-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditCourseForm(@PathVariable("id") Long id, Model model) {
        CourseDTO courseDto = courseService.getCourseDtoById(id);
        if (courseDto == null) {
            return "redirect:/courses";
        }

        model.addAttribute("courseDto", courseDto);
        loadCourseFormData(model);

        return "course/course-detail";
    }

    @PostMapping("/save")
    public String saveCourse(@Valid @ModelAttribute("courseDto") CourseDTO courseDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            loadCourseFormData(model);
            return "course/course-detail";
        }

        try {
            courseService.saveCourse(courseDto);
            redirectAttributes.addFlashAttribute("message", "Saved course successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/courses";
        } catch (Exception e) {
            model.addAttribute("message", "Error saving course: " + e.getMessage());
            model.addAttribute("alertType", "danger");
            loadCourseFormData(model);
            return "course/course-detail";
        }
    }

    @GetMapping("/status/{id}")
    public String changeStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {

        try {
            courseService.updateCourseStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "Updated course status successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating course status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/courses";
    }

    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id,
                               RedirectAttributes redirectAttributes) {

        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("message", "Deleted course successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete course: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/courses";
    }

    @GetMapping("/content/{id}")
    public String viewCourseContent(@PathVariable("id") Long id, Model model) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return "redirect:/courses";
        }

        List<Chapter> chapters = courseService.getChaptersByCourseId(id);
        Map<Integer, List<Lesson>> lessonMap = new HashMap<>();

        for (Chapter chapter : chapters) {
            lessonMap.put(chapter.getChapterId(),
                    courseService.getLessonsByChapterId(chapter.getChapterId()));
        }

        model.addAttribute("course", course);
        model.addAttribute("chapters", chapters);
        model.addAttribute("lessonMap", lessonMap);

        return "course/course-content";
    }

    @GetMapping("/chapter/new")
    public String showAddChapterForm(@RequestParam("courseId") Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/courses";
        }

        ChapterDTO chapterDto = new ChapterDTO();
        chapterDto.setCourseId(courseId);
        chapterDto.setStatus(1);

        model.addAttribute("chapterDto", chapterDto);
        model.addAttribute("course", course);

        return "course/chapter-detail";
    }

    @GetMapping("/chapter/edit/{id}")
    public String showEditChapterForm(@PathVariable("id") Integer id, Model model) {
        ChapterDTO chapterDto = courseService.getChapterDtoById(id);
        if (chapterDto == null) {
            return "redirect:/courses";
        }

        Course course = courseService.getCourseById(chapterDto.getCourseId());

        model.addAttribute("chapterDto", chapterDto);
        model.addAttribute("course", course);

        return "course/chapter-detail";
    }

    @PostMapping("/chapter/save")
    public String saveChapter(@Valid @ModelAttribute("chapterDto") ChapterDTO chapterDto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Course course = courseService.getCourseById(chapterDto.getCourseId());
        if (course == null) {
            return "redirect:/courses";
        }

        if (result.hasErrors()) {
            model.addAttribute("course", course);
            return "course/chapter-detail";
        }

        try {
            courseService.saveChapter(chapterDto);
            redirectAttributes.addFlashAttribute("message", "Saved chapter successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/courses/content/" + chapterDto.getCourseId();
        } catch (Exception e) {
            model.addAttribute("message", "Error saving chapter: " + e.getMessage());
            model.addAttribute("alertType", "danger");
            model.addAttribute("course", course);
            return "course/chapter-detail";
        }
    }

    @GetMapping("/chapter/delete/{id}")
    public String deleteChapter(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes) {

        Chapter chapter = courseService.getChapterById(id);
        if (chapter == null || chapter.getCourse() == null) {
            return "redirect:/courses";
        }

        Long courseId = chapter.getCourse().getCourseId();

        try {
            courseService.deleteChapter(id);
            redirectAttributes.addFlashAttribute("message", "Deleted chapter successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error deleting chapter: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/courses/content/" + courseId;
    }

    @GetMapping("/lesson/new")
    public String showAddLessonForm(@RequestParam("chapterId") Integer chapterId, Model model) {
        Chapter chapter = courseService.getChapterById(chapterId);
        if (chapter == null || chapter.getCourse() == null) {
            return "redirect:/courses";
        }

        LessonDTO lessonDto = new LessonDTO();
        lessonDto.setChapterId(chapterId);
        lessonDto.setStatus(1);
        lessonDto.setIsPreview(false);

        model.addAttribute("lessonDto", lessonDto);
        model.addAttribute("chapter", chapter);
        model.addAttribute("course", chapter.getCourse());

        return "course/lesson-detail";
    }

    @GetMapping("/lesson/edit/{id}")
    public String showEditLessonForm(@PathVariable("id") Integer id, Model model) {
        LessonDTO lessonDto = courseService.getLessonDtoById(id);
        if (lessonDto == null) {
            return "redirect:/courses";
        }

        Chapter chapter = courseService.getChapterById(lessonDto.getChapterId());
        if (chapter == null || chapter.getCourse() == null) {
            return "redirect:/courses";
        }

        model.addAttribute("lessonDto", lessonDto);
        model.addAttribute("chapter", chapter);
        model.addAttribute("course", chapter.getCourse());

        return "course/lesson-detail";
    }

    @PostMapping("/lesson/save")
    public String saveLesson(@Valid @ModelAttribute("lessonDto") LessonDTO lessonDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        Chapter chapter = courseService.getChapterById(lessonDto.getChapterId());
        if (chapter == null || chapter.getCourse() == null) {
            return "redirect:/courses";
        }

        if (result.hasErrors()) {
            model.addAttribute("chapter", chapter);
            model.addAttribute("course", chapter.getCourse());
            return "course/lesson-detail";
        }

        try {
            courseService.saveLesson(lessonDto);
            redirectAttributes.addFlashAttribute("message", "Saved lesson successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/courses/content/" + chapter.getCourse().getCourseId();
        } catch (Exception e) {
            model.addAttribute("message", "Error saving lesson: " + e.getMessage());
            model.addAttribute("alertType", "danger");
            model.addAttribute("chapter", chapter);
            model.addAttribute("course", chapter.getCourse());
            return "course/lesson-detail";
        }
    }

    @GetMapping("/lesson/delete/{id}")
    public String deleteLesson(@PathVariable("id") Integer id,
                               RedirectAttributes redirectAttributes) {

        LessonDTO lessonDto = courseService.getLessonDtoById(id);
        if (lessonDto == null) {
            return "redirect:/courses";
        }

        Chapter chapter = courseService.getChapterById(lessonDto.getChapterId());
        if (chapter == null || chapter.getCourse() == null) {
            return "redirect:/courses";
        }

        Long courseId = chapter.getCourse().getCourseId();

        try {
            courseService.deleteLesson(id);
            redirectAttributes.addFlashAttribute("message", "Deleted lesson successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error deleting lesson: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/courses/content/" + courseId;
    }

    private void loadCourseFormData(Model model) {
        model.addAttribute("categoryList", courseService.getAllCategories());
        model.addAttribute("levelList", courseService.getAllLevels());
        model.addAttribute("instructorList", courseService.getAllInstructors());
    }
}