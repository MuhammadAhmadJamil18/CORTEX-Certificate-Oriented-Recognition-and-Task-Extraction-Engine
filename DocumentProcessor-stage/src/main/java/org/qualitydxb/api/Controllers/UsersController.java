package org.qualitydxb.api.Controllers;

import jakarta.servlet.http.HttpSession;
import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.ChangePasswordRequest;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.infrastructure.LoggerService;
import org.qualitydxb.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private Users users;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userRequest) {
        try{
            return ResponseEntity.ok(users.login(userRequest));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User userRequest) {
        try{
            return ResponseEntity.ok(users.signup(userRequest));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<User> forgetPassword(@RequestBody User userRequest) {
        try{
            return ResponseEntity.ok(users.forgetPassword(userRequest));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<User> resetPassword(@RequestBody User userRequest) {
        try{
            return ResponseEntity.ok(users.resetPassword(userRequest));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> all(@RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try{
            return ResponseEntity.ok(users.getAll(clientId));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(Collections.singletonList(new User(ResponseCodes.SERVER_ERROR)));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<User> add(@RequestBody User userRequest, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
       try{
            return ResponseEntity.ok(users.add(userRequest, clientId, userId));
       } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
       }
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(@RequestBody User userRequest, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try{
            return ResponseEntity.ok(users.update(userRequest, clientId, userId));
        } catch(Exception ex){
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    // current user profile
    @GetMapping("/me")
    public ResponseEntity<User> me(@RequestAttribute("userId") Integer userId) {
        try {
            User u = users.getProfile(userId); // simple wrapper that calls dbService.findUserById
            return ResponseEntity.ok(u);
        } catch (Exception ex) {
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

    // change password
    @PostMapping("/change-password")
    public ResponseEntity<User> changePassword(@RequestBody ChangePasswordRequest req,
                                            @RequestAttribute("userId") Integer userId) {
        try {
            return ResponseEntity.ok(users.changePassword(req, userId));
        } catch (Exception ex) {
            LoggerService.log(ex, Project.USERS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new User(ResponseCodes.SERVER_ERROR));
        }
    }

}
