package org.qualitydxb.api.Controllers;

import org.qualitydxb.dal.Models.NotificationSetting;
import org.qualitydxb.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SettingsService svc;

    /* ───── GET current settings ───── */
    @GetMapping("/notification")
    public ResponseEntity<NotificationSetting> fetch(
            @RequestAttribute("clientId") Integer clientId) {

        return ResponseEntity.ok(svc.get(clientId));
    }

    /* ───── POST / PUT save settings ───── */
    @PostMapping("/notification")
    public ResponseEntity<NotificationSetting> save(
            @RequestBody NotificationSetting req,
            @RequestAttribute("clientId") Integer clientId) {

        return ResponseEntity.ok(svc.save(req, clientId));
    }
}
