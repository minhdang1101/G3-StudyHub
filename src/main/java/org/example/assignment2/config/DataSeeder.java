package org.example.assignment2.config;

import org.example.assignment2.model.*;
import org.example.assignment2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private SettingRepository settingRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private CommentRepository commentRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting data seeding...");

        // 1. Seed Settings (Parent Types)
        Setting userRoleType = getOrCreateSettingType("User Role", "System settings for user roles");
        Setting courseCategoryType = getOrCreateSettingType("Course Category", "System settings for course categories");
        getOrCreateSettingType("Post Status", "System settings for post statuses");

        // 2. Seed Child Settings (Roles)
        Setting roleAdmin = getOrCreateChildSetting("ROLE_ADMIN", "ADMIN", userRoleType, 1);
        Setting roleManager = getOrCreateChildSetting("ROLE_MANAGER", "MANAGER", userRoleType, 2);
        Setting roleTeacher = getOrCreateChildSetting("ROLE_TEACHER", "TEACHER", userRoleType, 3);
        Setting roleStudent = getOrCreateChildSetting("ROLE_STUDENT", "STUDENT", userRoleType, 4);

        // 3. Seed Categories
        Setting catIT = getOrCreateChildSetting("Information Technology", "IT", courseCategoryType, 1);
        Setting catBusiness = getOrCreateChildSetting("Business & Finance", "BUSINESS", courseCategoryType, 2);
        getOrCreateChildSetting("Graphics Design", "DESIGN", courseCategoryType, 3);

        // 4. Seed Users
        User admin = getOrCreateUser("Admin User", "admin", "admin@gmail.com", "1", roleAdmin);
        User manager = getOrCreateUser("Course Manager", "manager", "manager@gmail.com", "1", roleManager);
        User teacher = getOrCreateUser("John Doe", "teacher1", "teacher1@gmail.com", "1", roleTeacher);
        User student = getOrCreateUser("Jane Smith", "student1", "student1@gmail.com", "1", roleStudent);

        // 5. Seed Courses
        Course c1 = getOrCreateCourse("Java Backend Development", "Full stack Java development with Spring Boot", 
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97", new BigDecimal("1500000"), catIT, teacher, manager);
        getOrCreateCourse("Business Management 101", "Essentials of business management", 
                "https://images.unsplash.com/photo-1454165833767-027ffea70215", new BigDecimal("1200000"), catBusiness, teacher, manager);

        // 6. Seed Posts
        Post p1 = getOrCreatePost("Getting Started with Spring Boot", "This is a comprehensive guide to Spring Boot...", admin, "PUBLISHED", "https://images.unsplash.com/photo-1517694712202-14dd9538aa97");
        getOrCreatePost("Top 10 Design Trends in 2026", "Exploring the future of web design...", admin, "PUBLISHED", "https://images.unsplash.com/photo-1509395062183-67c5ad6faff9");

        // 7. Seed Comments
        if (commentRepo.count() <= 1) {
            for (int i = 1; i <= 12; i++) {
                Comment c = new Comment();
                c.setPost(p1);
                c.setUser(i % 2 == 0 ? student : teacher);
                c.setComment("Automated comment #" + i + " for pagination testing.");
                commentRepo.save(c);
            }
        }

        // 8. Seed Enrollments
        if (enrollmentRepo.count() == 0) {
            Enrollment e1 = new Enrollment();
            e1.setCourse(c1);
            e1.setUser(student);
            e1.setFullName(student.getFullName());
            e1.setEmail(student.getEmail());
            e1.setMobile(student.getMobile());
            e1.setFee(c1.getPrice());
            e1.setStatus("Approved");
            e1.setProgress(15.0);
            e1.setPaymentMethod("VNPAY");
            enrollmentRepo.save(e1);
        }

        System.out.println("Data seeding completed successfully.");
    }

    private Setting getOrCreateSettingType(String name, String desc) {
        return settingRepo.findByParentIsNull().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Setting s = new Setting();
                    s.setName(name);
                    s.setDescription(desc);
                    s.setStatus("Active");
                    return settingRepo.save(s);
                });
    }

    private Setting getOrCreateChildSetting(String name, String value, Setting parent, int order) {
        return settingRepo.findByTypeId(parent.getId()).stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Setting s = new Setting();
                    s.setName(name);
                    s.setValue(value);
                    s.setParent(parent);
                    s.setTypeId(parent.getId());
                    s.setOrderIndex(order);
                    s.setStatus("Active");
                    return settingRepo.save(s);
                });
    }

    private User getOrCreateUser(String fullName, String username, String email, String password, Setting role) {
        return userRepo.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setFullName(fullName);
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(password); // In real app, encode this
            u.setRole(role);
            u.setStatus("Active");
            u.setMobile("0987654321");
            return userRepo.save(u);
        });
    }

    private Course getOrCreateCourse(String title, String desc, String thumb, BigDecimal price, Setting cat, User instructor, User manager) {
        if (courseRepo.existsByTitle(title)) {
            return courseRepo.findByTitleContainingIgnoreCase(title).get(0);
        }
        Course c = new Course();
        c.setTitle(title);
        c.setDescription(desc);
        c.setThumbnailUrl(thumb);
        c.setPrice(price);
        c.setCategory(cat);
        c.setInstructor(instructor);
        c.setManager(manager);
        c.setStatus("Published");
        c.setLevel("All Levels");
        c.setDurationHours(40);
        return courseRepo.save(c);
    }

    private Post getOrCreatePost(String title, String content, User author, String status, String thumb) {
        if (postRepo.existsByTitle(title)) {
            return postRepo.searchPosts(null, null, title).get(0);
        }
        Post p = new Post();
        p.setTitle(title);
        p.setContent(content);
        p.setAuthor(author);
        p.setStatus(status);
        p.setThumbnailUrl(thumb);
        return postRepo.save(p);
    }
}
