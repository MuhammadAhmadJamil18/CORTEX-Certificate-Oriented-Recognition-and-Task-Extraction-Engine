package org.qualitydxb.notifications;

import org.qualitydxb.common.Enums.NotificationStatus;
import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.dal.Service.DBService;
import org.qualitydxb.infrastructure.SystemProperties;
import org.qualitydxb.notifications.Config.MailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class Notifications {

    @Autowired
    DBService dbService;
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    public Notification notify(Notification notificationRequest, int clientId, int userId) {
        notificationRequest.clientId=clientId;
        notificationRequest.userId=userId;
        final List<User> targets;
        if (notificationRequest.recipientId != null) {       // <-- single-recipient path
            targets      = List.of(dbService.findUserById(notificationRequest.recipientId));
            notificationRequest.notifyAll  = false;          // make sure older code cannot override
        } else {
            targets = dbService.getUsersByClientIdAndRole(clientId, notificationRequest.notifyAll);
        }
        EmailNotification emailNotification =new EmailNotification(new MailConfig().javaMailSender());
        WebNotification webNotification=new WebNotification(messagingTemplate);
        List<User> users=dbService.getUsersByClientIdAndRole(notificationRequest.clientId, notificationRequest.notifyAll);

        notificationRequest.status= NotificationStatus.DELIVERED.getNotificationStatus();
        notificationRequest.scheduleTime= LocalDateTime.now();

        if(Objects.equals(notificationRequest.notificationType, NotificationType.EMAIL.getNotificationType())){
            emailNotification.notifyViaMail(SystemProperties.getNotifyEmail(), notificationRequest, users);
        }
        else if(Objects.equals(notificationRequest.notificationType, NotificationType.WEB.getNotificationType())){
            webNotification.notifyViaWeb(notificationRequest, users);
        } else{
            emailNotification.notifyViaMail(SystemProperties.getNotifyEmail(), notificationRequest, users);
            webNotification.notifyViaWeb(notificationRequest, users);
        }
        notificationRequest=dbService.saveNotification(notificationRequest);
        return new Notification(notificationRequest, ResponseCodes.NOTIFICATION_DELIVERED);
    }

    public Notification schedule(Notification notificationRequest, int clientId, int userId) {
        notificationRequest.clientId=clientId;
        notificationRequest.userId=userId;
        notificationRequest.status= NotificationStatus.SCHEDULED.getNotificationStatus();
        notificationRequest=dbService.saveNotification(notificationRequest);

        return new Notification(notificationRequest, ResponseCodes.NOTIFICATION_SCHEDULED);
    }

    public Notification generalUserNotification(Notification notificationRequest, User user) {
       EmailNotification emailNotification =new EmailNotification(new MailConfig().javaMailSender());
       emailNotification.notifyViaMail(SystemProperties.getNotifyEmail(), notificationRequest, List.of(user));
       return new Notification(notificationRequest, ResponseCodes.NOTIFICATION_DELIVERED);
    }

    public List<Notification> getAll(int clientId, int userid) {
        return dbService.getAllNotifications(clientId, userid);
    }


    public List<Notification> forMe(int clientId,
                                    int userId,
                                    int userRole) {
        return dbService.getRelevantNotifications(clientId, userId, userRole);
    }


}