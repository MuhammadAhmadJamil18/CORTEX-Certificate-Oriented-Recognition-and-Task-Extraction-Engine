package org.qualitydxb.common.Enums;

public enum NotificationType {

    BOTH(1),
    EMAIL(2),
    WEB(3);

    private final Integer notificationType;

    NotificationType(Integer notificationType) {
        this.notificationType = notificationType;
    }

    public Integer getNotificationType() {
        return notificationType;
    }
}
