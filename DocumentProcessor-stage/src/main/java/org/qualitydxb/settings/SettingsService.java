package org.qualitydxb.settings;

import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.dal.Models.NotificationSetting;
import org.qualitydxb.dal.Service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class SettingsService {

    @Autowired
    private DBService db;

    /* ───────── fetch or build defaults ───────── */
    public NotificationSetting get(int clientId) {
        NotificationSetting s = db.getNotificationSetting(clientId);
        if (s == null) {
            s = new NotificationSetting();
            s.clientId         = clientId;
            s.daysBefore       = 1;
            s.notificationType = NotificationType.WEB.getNotificationType();
            s.emailSubject     = "Inspection due {{date}}";
            s.emailMessage     = "The certificate “{{document}}” must be inspected on {{date}}.";
        }
        return s;
    }

    /* ───────── create / update ───────── */
    public NotificationSetting save(NotificationSetting req, int clientId) {

        NotificationSetting s = db.getNotificationSetting(clientId);
        boolean create = (s == null);

        if (create) {
            s = new NotificationSetting();
            s.clientId   = clientId;
            s.createdAt  = LocalDateTime.now();
        }

        /* only mutable fields */
        s.daysBefore       = Objects.requireNonNullElse(req.daysBefore, 1);
        s.notificationType = Objects.requireNonNullElse(req.notificationType,
                                   NotificationType.WEB.getNotificationType());
        s.emailSubject     = req.emailSubject;
        s.emailMessage     = req.emailMessage;
        s.updatedAt        = LocalDateTime.now();

        return db.saveNotificationSetting(s);
    }
}
