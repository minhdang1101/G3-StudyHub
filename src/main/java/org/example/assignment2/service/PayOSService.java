package org.example.assignment2.service;

import org.example.assignment2.model.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PayOSService {

    @Autowired
    private PayOS payOS;

    @Autowired
    private EnrollmentService enrollmentService;

    public String createPayOSCheckoutUrl(Long enrollmentId, HttpServletRequest request) throws Exception {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        if (enrollment == null || !"Pending".equalsIgnoreCase(enrollment.getStatus())) {
            return null;
        }

        int amountInVnd = enrollment.getFee().intValue();
        if (amountInVnd < 2000) amountInVnd = 2000;

        String domain = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        long uniqueOrderCode = Long.parseLong(System.currentTimeMillis() / 1000 + String.valueOf(enrollment.getId()));

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(enrollment.getCourse().getTitle())
                .price((long) amountInVnd)
                .quantity(1)
                .build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(uniqueOrderCode)
                .amount((long) amountInVnd)
                .description("Thanh toan KH " + enrollment.getId())
                .returnUrl(domain + "/payment/payos/success/" + enrollment.getId())
                .cancelUrl(domain + "/payment/payos/cancel/" + enrollment.getId())
                .item(item)
                .build();

        CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

        return data.getCheckoutUrl();
    }
}
