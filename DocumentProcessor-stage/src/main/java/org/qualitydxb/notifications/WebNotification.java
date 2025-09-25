package org.qualitydxb.notifications;

import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
@Controller
public class WebNotification {

    private final SimpMessagingTemplate messagingTemplate;

    public WebNotification(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyViaWeb(Notification request, List<User> users) {
        for (User user : users) {
            messagingTemplate.convertAndSend("/socket/notification/" + user.userId, request);
        }
    }
}

