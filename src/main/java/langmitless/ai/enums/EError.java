package langmitless.ai.enums;

import lombok.Getter;

@Getter
public enum EError {
    BAD_REQUEST("Bad request!"),
    SERVICE_ERROR("Service error!"),
    EXISTED_BY_USERNAME("Username is existed!"),
    EXISTED_BY_EMAIL("Email is existed!"),
    EXISTED_BY_PHONE_NUMBER("Phone number is existed!"),
    EXISTED_BY_IDENTIFICATION_NUMBER("Phone number is existed!"),
    USER_NOT_EXISTED("User is not existed!"),
    INCORRECT_PASSWORD("Password is not correct!"),
    UNAUTHENTICATED("Account is not authenticated!"),
    UNAUTHORIZED("Account is not authenticated!"),
    ROLE_NOT_FOUND("Role not found!"),
    TOPIC_NOT_EXISTED("User is not existed!"),
    USER_IN_CALL("User is in a call!");

    private final String message;

    EError(String message) {
        this.message = message;
    }

}


