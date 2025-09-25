package org.qualitydxb.users;

import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.ChangePasswordRequest;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.dal.Service.DBService;
import org.qualitydxb.infrastructure.SystemProperties;
import org.qualitydxb.notifications.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class Users {

    @Autowired
    private JwtUtility jwt;

    @Autowired
    private DBService dbService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Encryption encryption;

    public User login(User userRequest) {
        User user = dbService.login(userRequest.userEmail);
        if(user == null) {
            return new User(userRequest, ResponseCodes.ACCOUNT_DOES_NOT_EXIST,null);
        }
        if(!user.isActive){
            return new User(userRequest, ResponseCodes.USER_NOT_ACTIVE,null);
        }
        if(!user.userPassword.equals(encryption.encrypt(userRequest.userPassword,SystemProperties.getEncryptionKey()))){
            return new User(userRequest, ResponseCodes.INCORRECT_PASSWORD,null);
        }

        return new User(user,ResponseCodes.LOGGED_IN, jwt.generateToken(user, false));
    }

    public User signup(User user) {
        return new User();
    }

    public User forgetPassword(User userRequest) {

        User user = dbService.login(userRequest.userEmail);
        if(user == null) {
            return new User(userRequest, ResponseCodes.ACCOUNT_DOES_NOT_EXIST,null);
        }

        Notification notification = new Notification();

        notification.notificationSubject="Reset Password";
        notification.notificationMessage="Click on the link to reset your password \n"+ SystemProperties.getFrontendUrl() +"/ResetPassword?token="+jwt.generateToken(user, true);

        notification=notifications.generalUserNotification(notification,user);

        if(notification.messageCode == ResponseCodes.NOTIFICATION_DELIVERED.getCode()){
            return new User(userRequest,ResponseCodes.EMAIL_SENT, null);
        }

        return new User(userRequest,ResponseCodes.SERVER_ERROR,null);
    }

    public User resetPassword(User userRequest) {
        if(jwt.isTokenExpired(userRequest.token)){
            return new User(userRequest, ResponseCodes.TOKEN_EXPIRED,null);
        }

        int userId= Math.toIntExact(jwt.extractUserId(userRequest.token));
        String userEmail=jwt.extractUsername(userRequest.token);

        User user = dbService.login(userEmail);
        if(user == null) {
            return new User(userRequest, ResponseCodes.USER_NOT_FOUND,null);
        }

        if(!user.userId.equals(userId)){
            return new User(userRequest, ResponseCodes.TOKEN_INVALID,null);
        }

        if(!userRequest.userPassword.equals(userRequest.confirmUserPassword)){
            return new User(userRequest, ResponseCodes.PASSWORDS_DO_NOT_MATCH,null);
        }

        user.userPassword=encryption.encrypt(userRequest.userPassword,SystemProperties.getEncryptionKey());
        user=dbService.updateUser(user);

        return new User(user, ResponseCodes.PASSWORD_RESET,null);
    }

    public List<User> getAll(int clientId) {
        return dbService.getAllUsers(clientId);
    }

    public User add(User userRequest, int clientId, int userId) {

        //add user permsission check here

        if(userRequest.userEmail == null || userRequest.userEmail.isEmpty()){
            return new User(userRequest, ResponseCodes.EMAIL_REQUIRED,null);
        }

        if(dbService.login(userRequest.userEmail) != null){
            return new User(userRequest, ResponseCodes.USER_ALREADY_EXISTS,null);
        }

        if(userRequest.userPassword == null || userRequest.userPassword.isEmpty()){
            return new User(userRequest, ResponseCodes.PASSWORD_REQUIRED,null);
        }

        if(!userRequest.userPassword.equals(userRequest.confirmUserPassword)){
            return new User(userRequest, ResponseCodes.PASSWORDS_DO_NOT_MATCH,null);
        }

        userRequest.createdAt= LocalDateTime.now();
        userRequest.clientId = clientId;
        userRequest.userPassword=encryption.encrypt(userRequest.userPassword,SystemProperties.getEncryptionKey());
        userRequest.userId = dbService.addUser(userRequest).userId;

        return new User(userRequest, ResponseCodes.USER_CREATED,null);
    }

    public User update(User userRequest, int clientId, int userId) {

        //add user permsission check here
        User user=dbService.login(userRequest.userEmail);
        if(user==null){
            return new User(userRequest, ResponseCodes.USER_NOT_FOUND,null);
        }

        if(userRequest.userName == null || userRequest.userName.isEmpty()){
            return new User(userRequest, ResponseCodes.USERNAME_REQUIRED,null);
        }

        if(userRequest.userRole == null){
            return new User(userRequest, ResponseCodes.USERROLE_REQUIRED,null);
        }

        if(userRequest.isActive == null){
            userRequest.isActive = true;
        }

        user.userName=userRequest.userName;
        user.userRole=userRequest.userRole;
        user.isActive=userRequest.isActive;
        userRequest=dbService.updateUser(user);

        return new User(userRequest, ResponseCodes.USER_UPDATED,null);
    }

    public User changePassword(ChangePasswordRequest req, int userId) {

        User user = dbService.findUserById(userId);
        if (user == null) {
            return new User(ResponseCodes.USER_NOT_FOUND);
        }

        String encCurr = encryption.encrypt(req.currentPassword, SystemProperties.getEncryptionKey());
        if (!Objects.equals(user.userPassword, encCurr)) {
            return new User(ResponseCodes.INCORRECT_PASSWORD);
        }

        if (!req.newPassword.equals(req.confirmPassword)) {
            return new User(ResponseCodes.PASSWORDS_DO_NOT_MATCH);
        }

        user.userPassword = encryption.encrypt(req.newPassword, SystemProperties.getEncryptionKey());
        user = dbService.updateUser(user);

        return new User(user, ResponseCodes.PASSWORD_RESET, null); // reuse existing enum
    }

    public User getProfile(int userId) {
        // Grab the user record
        User user = dbService.findUserById(userId);

        // If not found, return a lightweight error-response object
        if (user == null) {
            return new User(ResponseCodes.USER_NOT_FOUND);
        }

        /*  ⛑  SECURITY NOTE
        *  The caller only needs the visible profile fields.
        *  Blank-out the password (and any other sensitive fields you don’t want
        *  to expose) before returning.
        */
        user.userPassword = "";

        // You can wrap it in a response-code object if you prefer,
        // but the Profile page only needs the POJO itself:
        //   return new User(user, ResponseCodes.USER_FOUND, null);
        return user;
    }



}
