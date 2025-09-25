package org.qualitydxb.notifications;

import org.qualitydxb.common.Enums.NotificationStatus;
import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.dal.Service.DBService;
import org.qualitydxb.infrastructure.SystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
public class Scheduler {

    @Autowired private DBService         dbService;
    @Autowired private EmailNotification emailSender;
    @Autowired private WebNotification   webSender;

    @Scheduled(fixedRate = 30_000)
    public void triggerScheduledNotifications() {

        LocalDateTime now            = LocalDateTime.now();
        LocalDateTime oneMinuteLater = now.plusMinutes(1);

        

        List<Notification> due =
                dbService.findScheduledNotificationsBetween(now, oneMinuteLater);

        for (Notification n : due) {
            // printRow(n);

            List<User> targets;
            if (n.recipientId != null) {                       // single user
                User u = dbService.findUserById(n.recipientId);
                targets = (u != null) ? List.of(u) : List.of();
            } else {                                           // broadcast
                targets = dbService.getUsersByClientIdAndRole(
                              n.clientId, n.notifyAll);
            }
            System.out.printf("           %d recipient(s) after filtering%n",
                              targets.size());
            if (targets.isEmpty()) continue;

            if (Objects.equals(n.notificationType,
                               NotificationType.EMAIL.getNotificationType())) {
                System.out.println("           sending EMAIL");
                emailSender.notifyViaMail(
                        SystemProperties.getNotifyEmail(), n, targets);

            } else if (Objects.equals(n.notificationType,
                                      NotificationType.WEB.getNotificationType())) {
                System.out.println("           sending WEB");
                webSender.notifyViaWeb(n, targets);

            } else { // BOTH
                System.out.println("           sending EMAIL + WEB");
                emailSender.notifyViaMail(
                        SystemProperties.getNotifyEmail(), n, targets);
                webSender.notifyViaWeb(n, targets);
            }

            /* -------- Post-processing -------- */
            if (n.notificationFrequency == 0) {                // one-shot
                n.status = NotificationStatus.DELIVERED.getNotificationStatus();
            } else {                                           // recurring
                n.status       = NotificationStatus.SCHEDULED.getNotificationStatus();
                n.scheduleTime = calculateNextScheduledTime(n);
            }

            dbService.saveNotification(n);
        }
    }


    private LocalDateTime calculateNextScheduledTime(Notification n) {
        switch (n.notificationFrequency) {
            case 1:  return n.scheduleTime.plusDays(1);
            case 2:  return n.scheduleTime.plusWeeks(1);
            case 3:  return n.scheduleTime.plusMonths(1);
            default: return n.scheduleTime;   // unknown → no repeat
        }
    }

    private void printRow(Notification n) {
        System.out.printf(
            "           id=%d type=%d freq=%d sched=%s recip=%s notifyAll=%s%n",
            n.notificationId,
            n.notificationType,
            n.notificationFrequency,
            n.scheduleTime,
            String.valueOf(n.recipientId),
            n.notifyAll);
    }
}
