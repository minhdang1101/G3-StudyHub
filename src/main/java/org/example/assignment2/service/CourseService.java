package org.example.assignment2.service;

import jakarta.transaction.Transactional;
import org.example.assignment2.dto.ChapterDTO;
import org.example.assignment2.dto.CourseDTO;
import org.example.assignment2.dto.LessonDTO;
import org.example.assignment2.model.Chapter;
import org.example.assignment2.model.Course;
import org.example.assignment2.model.Lesson;
import org.example.assignment2.model.Setting;
import org.example.assignment2.model.User;
import org.example.assignment2.repository.ChapterRepository;
import org.example.assignment2.repository.CourseRepository;
import org.example.assignment2.repository.LessonRepository;
import org.example.assignment2.repository.SettingRepository;
import org.example.assignment2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ChapterRepository chapterRepo;

    @Autowired
    private LessonRepository lessonRepo;

    @Autowired
    private SettingRepository settingRepo;

    @Autowired
    private UserRepository userRepo;

    public List<Course> getFeaturedCourses() {
        return courseRepo.findTop6ByStatusOrderByCreatedAtDesc(1);
    }


    // =========================================================
    // GROUP 1: LIST / FORM DATA
    // =========================================================
    public List<Course> getCourses(String keyword, Integer categoryId, Integer instructorId, Integer status) {
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return courseRepo.searchCourses(key, categoryId, instructorId, status);
    }

    public List<Setting> getAllCategories() {
        // Tạm thời lấy tất cả setting cho dễ ghép project.
        // Khi nhóm bạn chốt type_id cho category thì đổi sang query riêng.
        return settingRepo.findAll();
    }

    public List<Setting> getAllLevels() {
        // Tạm thời lấy tất cả setting cho dễ ghép project.
        // Khi nhóm bạn chốt type_id cho level thì đổi sang query riêng.
        return settingRepo.findAll();
    }

    public List<User> getAllInstructors() {
        return userRepo.findAll();
    }

    // =========================================================
    // GROUP 2: COURSE CRUD
    // =========================================================
    public Course getCourseById(Integer id) {
        return courseRepo.findById(id).orElse(null);
    }

    public CourseDTO getCourseDtoById(Integer id) {
        Course course = courseRepo.findById(id).orElse(null);
        if (course == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setPrice(course.getPrice());
        dto.setDurationHours(course.getDurationHours());
        dto.setStatus(course.getStatus());

        if (course.getLevel() != null) {
            dto.setLevelId(course.getLevel().getId());
        }

        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
        }

        if (course.getInstructor() != null) {
            dto.setInstructorId(course.getInstructor().getId());
        }

        return dto;
    }

    @Transactional
    public void saveCourse(CourseDTO dto) {
        Course course;

        if (dto.getCourseId() != null) {
            course = courseRepo.findById(dto.getCourseId()).orElse(new Course());
        } else {
            course = new Course();
        }

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setPrice(dto.getPrice());
        course.setDurationHours(dto.getDurationHours());
        course.setStatus(dto.getStatus());

        if (dto.getLevelId() != null) {
            Setting level = settingRepo.findById(dto.getLevelId()).orElse(null);
            course.setLevel(level);
        } else {
            course.setLevel(null);
        }

        if (dto.getCategoryId() != null) {
            Setting category = settingRepo.findById(dto.getCategoryId()).orElse(null);
            course.setCategory(category);
        } else {
            course.setCategory(null);
        }

        if (dto.getInstructorId() != null) {
            User instructor = userRepo.findById(dto.getInstructorId()).orElse(null);
            course.setInstructor(instructor);
        } else {
            course.setInstructor(null);
        }

        courseRepo.save(course);
    }

    @Transactional
    public void updateCourseStatus(Integer id, Integer status) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setStatus(status);
        courseRepo.save(course);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        courseRepo.deleteById(id);
    }

    // =========================================================
    // GROUP 3: CHAPTER / LESSON
    // =========================================================
    public List<Chapter> getChaptersByCourseId(Integer courseId) {
        return chapterRepo.findByCourseCourseIdOrderByOrderIndexAsc(courseId);
    }

    public Chapter getChapterById(Integer chapterId) {
        return chapterRepo.findById(chapterId).orElse(null);
    }

    public ChapterDTO getChapterDtoById(Integer chapterId) {
        Chapter chapter = chapterRepo.findById(chapterId).orElse(null);
        if (chapter == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setChapterId(chapter.getChapterId());
        dto.setCourseId(chapter.getCourse().getCourseId());
        dto.setTitle(chapter.getTitle());
        dto.setDescription(chapter.getDescription());
        dto.setOrderIndex(chapter.getOrderIndex());
        dto.setStatus(chapter.getStatus());

        return dto;
    }

    @Transactional
    public void saveChapter(ChapterDTO dto) {
        Chapter chapter;

        if (dto.getChapterId() != null) {
            chapter = chapterRepo.findById(dto.getChapterId()).orElse(new Chapter());
        } else {
            chapter = new Chapter();
        }

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        chapter.setCourse(course);
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrderIndex(dto.getOrderIndex());
        chapter.setStatus(dto.getStatus());

        chapterRepo.save(chapter);
    }

    @Transactional
    public void deleteChapter(Integer chapterId) {
        chapterRepo.deleteById(chapterId);
    }

    public List<Lesson> getLessonsByChapterId(Integer chapterId) {
        return lessonRepo.findByChapterChapterIdOrderByOrderIndexAsc(chapterId);
    }

    public LessonDTO getLessonDtoById(Integer lessonId) {
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null) {
            return null;
        }

        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getLessonId());
        dto.setChapterId(lesson.getChapter().getChapterId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setIsPreview(lesson.getIsPreview());
        dto.setStatus(lesson.getStatus());

        return dto;
    }

    @Transactional
    public void saveLesson(LessonDTO dto) {
        Lesson lesson;

        if (dto.getLessonId() != null) {
            lesson = lessonRepo.findById(dto.getLessonId()).orElse(new Lesson());
        } else {
            lesson = new Lesson();
        }

        Chapter chapter = chapterRepo.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setContentType(dto.getContentType());
        lesson.setContentUrl(dto.getContentUrl());
        lesson.setDurationMinutes(dto.getDurationMinutes());
        lesson.setOrderIndex(dto.getOrderIndex());
        lesson.setIsPreview(dto.getIsPreview());
        lesson.setStatus(dto.getStatus());

        lessonRepo.save(lesson);
    }

    @Transactional
    public void deleteLesson(Integer lessonId) {
        lessonRepo.deleteById(lessonId);
    }

    // =========================================================
    // GROUP 4: AUTHORIZATION HOOKS
    // =========================================================
    public boolean isAssignedCourse(Integer currentUserId, Integer courseId) {
        if (currentUserId == null || courseId == null) {
            return false;
        }

        Course course = getCourseById(courseId);
        if (course == null || course.getInstructor() == null) {
            return false;
        }

        return currentUserId.equals(course.getInstructor().getId());
    }

    public boolean canViewCourse(Integer currentUserId, String currentRole, Integer courseId) {
        if (currentRole == null || currentRole.isBlank()) {
            return true; // TODO: teammate auth có thể siết lại sau
        }

        if ("Admin".equalsIgnoreCase(currentRole)) {
            return true;
        }

        if ("Manager".equalsIgnoreCase(currentRole)) {
            return isAssignedCourse(currentUserId, courseId);
        }

        return false;
    }

    public boolean canEditCourse(Integer currentUserId, String currentRole, Integer courseId) {
        if (currentRole == null || currentRole.isBlank()) {
            return true; // TODO: teammate auth có thể siết lại sau
        }

        if ("Admin".equalsIgnoreCase(currentRole)) {
            return true;
        }

        if ("Manager".equalsIgnoreCase(currentRole)) {
            return isAssignedCourse(currentUserId, courseId);
        }

        return false;
    }

    public boolean canAccessChapter(Integer currentUserId, String currentRole, Integer chapterId) {
        Chapter chapter = getChapterById(chapterId);
        if (chapter == null || chapter.getCourse() == null) {
            return false;
        }

        return canViewCourse(currentUserId, currentRole, chapter.getCourse().getCourseId());
    }

    public boolean canAccessLesson(Integer currentUserId, String currentRole, Integer lessonId) {
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null || lesson.getChapter() == null || lesson.getChapter().getCourse() == null) {
            return false;
        }

        return canViewCourse(currentUserId, currentRole, lesson.getChapter().getCourse().getCourseId());
    }
}