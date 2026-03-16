package org.example.assignment2.controller;

import org.example.assignment2.model.Enrollment;
import org.example.assignment2.model.User;
import org.example.assignment2.service.EnrollmentService;
import org.example.assignment2.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.assignment2.service.EmailService;
import org.example.assignment2.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.assignment2.service.PayOSService;

@Controller
public class UserEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private PayOSService payOSService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/enroll/{courseId}")
    public String showEnrollForm(@PathVariable Long courseId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        Enrollment enrollment = enrollmentService.createInitialEnrollment(courseId, currentUser);
        if (enrollment == null) return "redirect:/";

        model.addAttribute("enrollment", enrollment);
        model.addAttribute("course", enrollment.getCourse());
        return "enrollment/learning-enroll";
    }

    @PostMapping("/enroll/submit")
    public String submitEnrollment(@ModelAttribute Enrollment enrollment, @RequestParam("courseId") Long courseId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        // Save initial enrollment details first
        enrollmentService.processEnrollmentSubmission(enrollment, courseId);
        
        // If guest, create account and send email
        if (currentUser == null) {
            processAutoRegistrationAndEmail(enrollment);
        } else {
            enrollment.setUser(currentUser);
            enrollmentService.updateEnrollment(enrollment);
        }

        if ("Internet Banking".equals(enrollment.getPaymentMethod()) || "VNPay".equals(enrollment.getPaymentMethod()) || "VNPAY".equals(enrollment.getPaymentMethod())) {
            return "redirect:/payment/vnpay/checkout?id=" + enrollment.getId();
        } else {
            return "redirect:/payment/payos/checkout?id=" + enrollment.getId();
        }
    }



    @GetMapping("/payment/payos/checkout")

    public String createPayOSLink(@RequestParam("id") Long id, HttpServletRequest request) {
        try {
            String checkoutUrl = payOSService.createPayOSCheckoutUrl(id, request);
            if (checkoutUrl == null) {
                return "redirect:/my-enrollments";
            }
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            System.err.println("Lỗi PayOS: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/my-enrollments?error=payos_failed";
        }
    }

    @GetMapping("/payment/payos/success/{enrollmentId}")
    public String payOSSuccess(@PathVariable("enrollmentId") Long enrollmentId) {
        enrollmentService.markEnrollmentAsPaid(enrollmentId);
        return "redirect:/my-enrollments?payment=success";
    }

    @GetMapping("/payment/payos/cancel/{enrollmentId}")
    public String payOSCancel(@PathVariable("enrollmentId") Long enrollmentId) {
        return "redirect:/my-enrollments?payment=cancel";
    }
    

    @GetMapping("/payment/vnpay/checkout")

    public String createVnPayLink(@RequestParam("id") Long id, HttpServletRequest request) {
        try {
            String vnpayUrl = vnPayService.createOrder(id, request);
            return "redirect:" + vnpayUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/my-enrollments?error=vnpay_failed";
        }
    }

    @GetMapping("/payment/vnpay/success")
    public String vnPaySuccess(
            @RequestParam("enrollmentId") Long enrollmentId,
            @RequestParam("vnp_ResponseCode") String responseCode) {
        if ("00".equals(responseCode)) {
            enrollmentService.markEnrollmentAsPaid(enrollmentId);
            return "redirect:/my-enrollments?payment=success";
        } else {
            return "redirect:/my-enrollments?payment=cancel";
        }
    }

    @GetMapping("/my-enrollments")
    public String showMyEnrollments(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByUserId(currentUser.getId().longValue());
        model.addAttribute("enrollments", enrollments);
        return "enrollment/my-enrollments";
    }


    public void markEnrollmentAsPaid(Long enrollmentId) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        
        if (enrollment != null && "Pending".equalsIgnoreCase(enrollment.getStatus())) {
            
            enrollment.setStatus("Paid");
            enrollment.setLastUpdated(LocalDateTime.now());
            enrollmentService.updateEnrollment(enrollment);
            processAutoRegistrationAndEmail(enrollment);
        }
    }

    private void processAutoRegistrationAndEmail(Enrollment enrollment) {
        String learnerEmail = enrollment.getEmail(); 
        User existingLearner = userService.findByEmail(learnerEmail);
        String generatedPassword = null;

        if (existingLearner == null) {
            User newLearner = new User();
            newLearner.setEmail(learnerEmail);
            newLearner.setFullName(enrollment.getFullName());
            newLearner.setMobile(enrollment.getMobile());
            newLearner.setStatus("Active");
            generatedPassword = UUID.randomUUID().toString().substring(0, 8) + "@1A";
            newLearner.setPassword(generatedPassword); 
            
            // Set default role (3 - Member/Learner)
            org.example.assignment2.model.Setting role = new org.example.assignment2.model.Setting();
            role.setId(3);
            newLearner.setRole(role);

            userService.saveUser(newLearner);
            enrollment.setUser(newLearner);
        } else {
            enrollment.setUser(existingLearner);
        }
        
        enrollmentService.updateEnrollment(enrollment);

        emailService.sendAccessInfoToLearner(learnerEmail, enrollment, generatedPassword);

        User buyer = enrollment.getUser(); // Usually the same as learner for guests
        if (buyer != null && buyer.getEmail() != null && !buyer.getEmail().equalsIgnoreCase(learnerEmail)) {
            emailService.sendReceiptToBuyer(buyer.getEmail(), enrollment);
        }
    }

}
