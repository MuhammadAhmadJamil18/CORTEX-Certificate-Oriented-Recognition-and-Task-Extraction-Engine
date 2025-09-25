package org.qualitydxb.api.Controllers;

import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.DocumentFormat;
import org.qualitydxb.dal.Models.DocumentType;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.infrastructure.LoggerService;
import org.qualitydxb.reports.Reports;
import org.qualitydxb.common.Models.ReportsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    @Autowired
    private Reports report;

    @PostMapping("/fetch")
    public ResponseEntity<ReportsRequest> fetch(
        @RequestBody ReportsRequest req,
        @RequestAttribute("clientId")   Integer clientId,
        @RequestAttribute("userId")     Integer userId,
        @RequestAttribute("userRole")   Integer userRole
    ) {
        try {
            req.clientId = clientId;
            req.userId   = userId;
            return ResponseEntity.ok(report.fetch(req, clientId, userId, userRole));
        } catch(Exception ex) {
            LoggerService.log(ex, Project.REPORTS, LogTag.ERROR);
            return ResponseEntity.status(500)
                                 .body(new ReportsRequest(ResponseCodes.SERVER_ERROR));
        }
    }

    @GetMapping("/filters")
    public ResponseEntity<List<DocumentType>> filters(
        @RequestAttribute("clientId") Integer clientId
    ) {
        try {
            return ResponseEntity.ok(report.filters(clientId));
        } catch(Exception ex) {
            LoggerService.log(ex, Project.REPORTS, LogTag.ERROR);
            return ResponseEntity.status(500)
                                 .body(Collections.singletonList(
                                     new DocumentType(ResponseCodes.SERVER_ERROR)
                                 ));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ReportsRequest> statistics(@RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId, @RequestAttribute(name = "userRole", required = true) Integer userRole) {
        try{
            return ResponseEntity.ok(report.statistics(clientId,userId, userRole));
        } catch(Exception ex){
            LoggerService.log(ex, Project.REPORTS, LogTag.ERROR);
            return ResponseEntity.status(401).body(new ReportsRequest(ResponseCodes.SERVER_ERROR));
        }
    }
}
