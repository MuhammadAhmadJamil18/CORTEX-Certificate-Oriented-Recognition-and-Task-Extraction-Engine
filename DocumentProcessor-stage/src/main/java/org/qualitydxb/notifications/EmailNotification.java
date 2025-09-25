package org.qualitydxb.notifications;

import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailNotification {

    private final JavaMailSender mailSender;

    public EmailNotification(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyViaMail(String fromEmail, Notification request, List<User> users) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);

        for (User user : users) {
            message.setTo(user.userEmail);
            message.setSubject(request.notificationSubject);
            message.setText(request.notificationMessage);
            mailSender.send(message);
        }
    }
}
