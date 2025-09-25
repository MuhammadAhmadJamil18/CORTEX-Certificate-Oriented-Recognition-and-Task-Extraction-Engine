package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import org.qualitydxb.common.Enums.ResponseCodes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification", schema = "qualitydxb")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationid")
    public Integer notificationId; // Primary Key

    @Column(name = "clientid")
    public Integer clientId; // Client ID (FK)

    @Column(name = "userid")
    public Integer userId; // User ID (FK)

    @Column(name = "notificationtype")
    public Integer notificationType; // Notification Type (FK)

    @Column(name = "notificationfrequency")
    public Integer notificationFrequency; // Notification Frequency (FK)

    @Column(name = "notifyall")
    public Boolean notifyAll; // Notify All Flag

    @Column(name = "subject")
    public String notificationSubject;

    @Column(name = "message")
    public String notificationMessage;

    @Column(name = "scheduletime")
    public LocalDateTime scheduleTime; // Schedule Time

    public String timezone; // Timezone

    public Integer status; // Status of Notification

    @Column(name = "createdat")
    public LocalDateTime createdAt; // Created At

    //single user that must receive this notice;  NULL → fall back to old broadcast logic 
    @Column(name = "recipientid")
    public Integer recipientId;

    @Transient
    public int messageCode;
    @Transient
    public String message;
    @Transient
    public String notifiedBy;

    @Transient
    private String recipientName;  

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public Notification(){}

    public Notification(ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
    }

    public Notification(String subject, String message){
        this.notificationSubject=subject;
        this.notificationMessage=message;
    }

    public Notification(Notification notification, ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
        this.notificationId=notification.notificationId;
        this.clientId=notification.clientId;
        this.userId=notification.userId;
        this.notificationType=notification.notificationType;
        this.notificationFrequency=notification.notificationFrequency;
        this.notifyAll=notification.notifyAll;
        this.notificationSubject=notification.notificationSubject;
        this.notificationMessage=notification.notificationMessage;
        this.scheduleTime=notification.scheduleTime;
        this.timezone=notification.timezone;
        this.status=notification.status;
        this.createdAt=notification.createdAt;
    }
}
