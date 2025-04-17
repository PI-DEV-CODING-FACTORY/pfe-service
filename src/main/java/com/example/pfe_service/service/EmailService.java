package com.example.pfe_service.service;

import com.example.pfe_service.exception.EmailSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;

    public void sendInterviewInvitation(String to, LocalDateTime interviewDateTime, String message) {
        log.info("Preparing to send interview invitation email to: {}", to);
        
        try {
            validateEmailParameters(to, interviewDateTime, message);
            
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Interview Invitation for PFE Project");
            helper.setText(buildEmailContent(interviewDateTime, message), true); // true for HTML content
            
            emailSender.send(mimeMessage);
            log.info("Interview invitation email sent successfully to {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send interview invitation email to {}: {}", to, e.getMessage());
            throw new EmailSendException("Failed to send interview invitation email", e);
        }
    }
    
    private void validateEmailParameters(String to, LocalDateTime interviewDateTime, String message) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Email recipient cannot be null or empty");
        }
        if (interviewDateTime == null) {
            throw new IllegalArgumentException("Interview date/time cannot be null");
        }
        if (interviewDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interview date/time cannot be in the past");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
    }
    
    private String buildEmailContent(LocalDateTime interviewDateTime, String message) {
        String formattedDateTime = interviewDateTime.format(
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")
        );
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Interview Invitation for PFE Project</h2>
                <p>Dear Student,</p>
                <p>You have been invited for an interview regarding your PFE project.</p>
                <div style="margin: 20px 0; padding: 15px; background-color: #f5f5f5; border-radius: 5px;">
                    <h3 style="margin-top: 0;">Interview Details</h3>
                    <p><strong>Date and Time:</strong> %s</p>
                </div>
                <div style="margin: 20px 0;">
                    <h3>Additional Message</h3>
                    <p>%s</p>
                </div>
                <p>Please make sure to be available at the specified time.</p>
                <p>Best regards,<br>The PFE Team</p>
            </body>
            </html>
            """, formattedDateTime, message.replace("\n", "<br>"));
    }
} 