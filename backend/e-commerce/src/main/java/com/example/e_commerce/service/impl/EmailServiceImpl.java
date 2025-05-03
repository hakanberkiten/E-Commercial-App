package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOrderConfirmation(Orders order) {
        try {
            log.info("Preparing to send order confirmation email to: {}", order.getEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(order.getEmail());
            helper.setSubject("Siparişiniz Onaylandı - Sipariş #" + order.getOrderId());
            helper.setFrom("ecommerce.x.07.07@gmail.com");
            
            String content = buildOrderEmailContent(order);
            helper.setText(content, true);
            
            log.info("Attempting to send email to: {}", order.getEmail());
            mailSender.send(message);
            log.info("Email sent successfully to: {}", order.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error when sending email: {}", e.getMessage(), e);
        }
    }
    
    private String buildOrderEmailContent(Orders order) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee;'>");
        content.append("<h1 style='color: #6a55c2;'>Your Order has been Confirmed</h1>");
        content.append("<p>Hello ").append(order.getUser() != null ? order.getUser().getFirstName() : "Valued Customer").append(",</p>");
        content.append("<p>Your order has been successfully created. Here are your order details:</p>");
        
        content.append("<div style='background-color: #f9f9f9; padding: 15px; margin: 15px 0;'>");
        content.append("<p><strong>Order Number:</strong> #").append(order.getOrderId()).append("</p>");
        content.append("<p><strong>Order Date:</strong> ").append(order.getOrderDate()).append("</p>");
        content.append("<p><strong>Total Amount:</strong> $").append(order.getTotalAmount()).append("</p>");
        content.append("</div>");
        
        content.append("<table style='width: 100%; border-collapse: collapse;'>");
        content.append("<tr style='background-color: #f2f2f2;'>");
        content.append("<th style='padding: 10px; text-align: left;'>Product</th>");
        content.append("<th style='padding: 10px; text-align: center;'>Quantity</th>");
        content.append("<th style='padding: 10px; text-align: right;'>Price</th>");
        content.append("</tr>");
        
        for (OrderItem item : order.getItems()) {
            content.append("<tr>");
            content.append("<td style='padding: 10px; border-bottom: 1px solid #eee;'>")
                  .append(item.getProduct().getProductName())
                  .append("</td>");
            content.append("<td style='padding: 10px; text-align: center; border-bottom: 1px solid #eee;'>")
                  .append(item.getQuantityInOrder())
                  .append("</td>");
            content.append("<td style='padding: 10px; text-align: right; border-bottom: 1px solid #eee;'>$")
                  .append(item.getOrderedProductPrice())
                  .append("</td>");
            content.append("</tr>");
        }
        
        content.append("</table>");
        
        content.append("<p style='margin-top: 30px;'>If you have any questions about your order, please contact us.</p>");
        content.append("<p>Thank you for your purchase!</p>");
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }
}