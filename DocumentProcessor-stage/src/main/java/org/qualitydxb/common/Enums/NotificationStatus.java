package org.qualitydxb.common.Enums;

public enum NotificationStatus {

    SCHEDULED(1),
    DELIVERED(2);

    private final Integer notificationStatus;

    NotificationStatus(Integer notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public Integer getNotificationStatus() {
        return notificationStatus;
    }
}
