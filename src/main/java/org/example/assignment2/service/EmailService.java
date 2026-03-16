package org.example.assignment2.service;

import org.example.assignment2.model.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendReceiptToBuyer(String buyerEmail, Enrollment enrollment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(buyerEmail);
        message.setSubject("Xác nhận thanh toán thành công - " + enrollment.getCourse().getTitle());
        message.setText("Cảm ơn bạn đã thanh toán thành công khóa học " + enrollment.getCourse().getTitle() 
                + " cho học viên " + enrollment.getFullName() 
                + ".\nMã đơn hàng: " + enrollment.getId());
        mailSender.send(message);
    }

    public void sendAccessInfoToLearner(String learnerEmail, Enrollment enrollment, String randomPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(learnerEmail);
        message.setSubject("Thông tin truy cập khóa học - " + enrollment.getCourse().getTitle());
        
        StringBuilder text = new StringBuilder();
        text.append("Chào ").append(enrollment.getFullName()).append(",\n\n");
        text.append("Bạn đã được đăng ký thành công khóa học: ").append(enrollment.getCourse().getTitle()).append("\n\n");
        
        if (randomPassword != null) {
            text.append("Vì bạn chưa có tài khoản, hệ thống đã tự động tạo tài khoản cho bạn.\n");
            text.append("Tên đăng nhập: ").append(learnerEmail).append("\n");
            text.append("Mật khẩu: ").append(randomPassword).append("\n");
            text.append("Vui lòng đăng nhập và đổi mật khẩu sớm nhất nhé!\n\n");
        } else {
            text.append("Hãy đăng nhập bằng tài khoản hiện tại của bạn để bắt đầu học nhé.\n\n");
        }
        
        text.append("Chúc bạn học tốt!");
        mailSender.send(message);
    }

    public void sendAccountEmail(String toEmail, String password) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Your StudyHub Account");

        message.setText(
                "Your account has been created successfully.\n\n" +
                        "Login Information:\n" +
                        "Email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please login and change your password.\n\n" +
                        "StudyHub Team"
        );

        mailSender.send(message);
    }

    public void sendOtp(String toEmail,String otp){

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("StudyHub Password Reset");
        message.setText("Your OTP code: " + otp);

        mailSender.send(message);
    }
}
