package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import org.qualitydxb.common.Enums.ResponseCodes;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "qualitydxb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    public Integer userId; // Primary Key

    @Column(name = "userrole")
    public Integer userRole;

    @Column(name = "clientid")
    public Integer clientId;

    @Column(name = "username")
    public String userName; // Username

    @Column(name = "useremail")
    public String userEmail; // User Email

    @Column(name = "userpassword")
    public String userPassword; // User Password

    @Column(name = "isactive")
    public Boolean isActive; // Active Status

    @Column(name = "createdat")
    public LocalDateTime createdAt; // Creation Timestamp


    @Transient
    public Boolean loggedIn; // Logged In Status
    @Transient
    public String message; // User Email
    @Transient
    public Integer messageCode; // User Email
    @Transient
    public String token;
    @Transient
    public String confirmUserPassword;

    public User(){}

    public User(Integer clientId,Integer userId, String email, String password, Integer userRole) {
        this.clientId = clientId;
        this.userId=userId;
        this.userEmail = email;
        this.userPassword = password;
        this.userRole = userRole;
        this.loggedIn=false;
        this.isActive=true;
    }

    public User(User user, ResponseCodes response, String token){
        this.message=response.getValue();
        this.messageCode=response.getCode();
        if(Objects.requireNonNull(response) ==ResponseCodes.LOGGED_IN || Objects.requireNonNull(response) ==ResponseCodes.USER_CREATED || Objects.requireNonNull(response) ==ResponseCodes.USER_UPDATED){
            this.userEmail= user.userEmail;
            this.userRole=user.userRole;
            this.userId=user.userId;
            this.userName=user.userName;
            this.clientId=user.clientId;
            this.loggedIn= Objects.requireNonNull(response) == ResponseCodes.LOGGED_IN;
            this.token=token;
            this.isActive=user.isActive;
            this.createdAt=user.createdAt;
        }
    }

    public User(ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
    }
}
