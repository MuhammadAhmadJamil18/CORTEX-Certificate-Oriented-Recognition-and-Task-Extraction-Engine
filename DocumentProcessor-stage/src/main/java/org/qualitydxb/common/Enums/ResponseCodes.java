package org.qualitydxb.common.Enums;

public enum ResponseCodes {

    //general errors

    //user errors
    LOGGED_IN(1000, "Login successful."),
    INCORRECT_PASSWORD(1001, "Incorrect password."),
    ACCOUNT_DOES_NOT_EXIST(1002, "Account does not exist."),
    USER_NOT_FOUND(1003, "User not found."),
    USER_ALREADY_EXISTS(1004, "User already exists."),
    USER_NOT_LOGGED_IN(1005, "User not logged in."),
    USER_NOT_AUTHORIZED(1006, "User not authorized."),
    TOKEN_EXPIRED(1007, "Token is expired."),
    TOKEN_INVALID(1008, "Token is invalid."),
    TOKEN_NOT_FOUND(1009, "Token is required."),
    USER_NOT_ACTIVE(1010,"User not active"),
    EMAIL_REQUIRED(1011,"Email is required."),
    PASSWORD_REQUIRED(1012,"Password is required."),
    PASSWORDS_DO_NOT_MATCH(1013,"Passwords do not match."),
    USER_CREATED(1014,"User created."),
    USERNAME_REQUIRED(1015,"Username is required."),
    USER_UPDATED(1016,"User updated."),
    USERROLE_REQUIRED(1017,"User role is required."),
    EMAIL_SENT(1018,"Reset Password email sent."),
    PASSWORD_RESET(1019,"Password reset success."),

    //notification errors

    //report errors

    //processing errors
    PROCESSOR_NOT_FOUND(5001, "Processor not found for extension "),
    CANNOT_PROCESS_DOCUMENT(5002, "Cannot process the document."),
    MAX_CONCURRENT_FILES_EXCEEDED(5003, "Maximum file upload limit reached."),

    //server error
    SERVER_ERROR(5004,"Server cannot process the request."),
    DATABASE_ERROR(5005,"Database error."),

    //processing response
    DOCUMENT_PROCESSED(2000, "Document processed."),

    //notification response
    NOTIFICATION_DELIVERED(2000, "Notification delivered."),
    NOTIFICATION_SCHEDULED(2001, "Notification scheduled.");

    private final String value;
    private final Integer code;

    ResponseCodes(Integer code, String value) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }
    public Integer getCode() {
        return code;
    }
}
