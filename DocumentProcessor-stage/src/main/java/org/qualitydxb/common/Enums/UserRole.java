package org.qualitydxb.common.Enums;

public enum UserRole {

    ADMIN(1),
    SALESMAN(2);

    private final Integer role;

    UserRole(Integer role) {
        this.role = role;
    }

    public Integer getRole() {
        return role;
    }
}
