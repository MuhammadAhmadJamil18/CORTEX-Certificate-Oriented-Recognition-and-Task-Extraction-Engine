package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificationsettings", schema = "qualitydxb")
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settingid")
    public Integer settingId;        // PK

    @Column(name = "clientid")
    public Integer clientId;         // FK → clients

    @Column(name = "daysbefore")
    public Integer daysBefore;       // # days prior to next inspection

    @Column(name = "notificationtype")
    public Integer notificationType; // 1 Both, 2 Email, 3 Web

    @Column(name = "emailsubject")
    public String emailSubject;

    @Column(name = "emailmessage", columnDefinition = "TEXT")
    public String emailMessage;

    @Column(name = "createdat")
    public LocalDateTime createdAt;

    @Column(name = "updatedat")
    public LocalDateTime updatedAt;
}
