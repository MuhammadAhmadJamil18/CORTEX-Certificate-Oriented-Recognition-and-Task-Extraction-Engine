package org.qualitydxb.common.Enums;

public enum NotificationFrequency {

    DAILY(1),
    WEEKLY(2),
    MONTHLY(3);

    private final Integer notificationFrequency;

    NotificationFrequency(Integer notificationFrequency) {
        this.notificationFrequency = notificationFrequency;
    }

    public Integer getNotificationType() {
        return notificationFrequency;
    }
}
