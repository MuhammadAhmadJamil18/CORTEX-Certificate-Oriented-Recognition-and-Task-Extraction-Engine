package org.qualitydxb.api.Controllers;

import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.infrastructure.LoggerService;
import org.qualitydxb.notifications.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private Notifications notification;

    @PostMapping("/notify")
    public ResponseEntity<Notification> notify(@RequestBody Notification notificationRequest, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try{
            return ResponseEntity.ok(notification.notify(notificationRequest, clientId,userId));
        } catch(Exception ex){
            LoggerService.log(ex, Project.NOTIFICATIONS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new Notification(ResponseCodes.SERVER_ERROR));
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<Notification> schedule(@RequestBody Notification notificationRequest, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try{
            return ResponseEntity.ok(notification.schedule(notificationRequest, clientId,userId));
        } catch(Exception ex){
            LoggerService.log(ex, Project.NOTIFICATIONS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new Notification(ResponseCodes.SERVER_ERROR));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> all(@RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try{
            return ResponseEntity.ok(notification.getAll(clientId, userId));
        } catch(Exception ex){
            LoggerService.log(ex, Project.NOTIFICATIONS, LogTag.ERROR);
            return ResponseEntity.status(401).body(Collections.singletonList(new Notification(ResponseCodes.SERVER_ERROR)));
        }
    }

    @GetMapping("/forMe")
    public ResponseEntity<List<Notification>> forMe(
            @RequestAttribute("clientId") Integer clientId,
            @RequestAttribute("userId")   Integer userId,
            @RequestAttribute("userRole") Integer userRole) {

        try {
            return ResponseEntity.ok(
                    notification.forMe(clientId, userId, userRole));
        } catch (Exception ex) {
            LoggerService.log(ex, Project.NOTIFICATIONS, LogTag.ERROR);
            return ResponseEntity.status(500).body(
                    Collections.singletonList(
                            new Notification(ResponseCodes.SERVER_ERROR)));
        }
    }


}
