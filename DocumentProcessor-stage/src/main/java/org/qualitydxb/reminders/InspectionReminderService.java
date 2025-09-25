package org.qualitydxb.reminders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.common.Enums.UserRole;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.NotificationSetting;
import org.qualitydxb.dal.Models.SearchTerm;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.dal.Service.DBService;
import org.qualitydxb.infrastructure.SystemProperties;
import org.qualitydxb.notifications.Notifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InspectionReminderService {

    private final DBService db;
    private final Notifications notifications;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

    @Transactional
    public void handle(ProcessRequest req) {

        /* ───────── 1. pull the ISO date ───────── */
        String dateTxt = extract(req, "next_date_of_inspection");
        if (dateTxt == null || dateTxt.equalsIgnoreCase("no data found"))
            return;

        LocalDate next;
        try {
            next = LocalDate.parse(dateTxt, ISO);
        } catch (DateTimeParseException ex) {
            return; // bad format → ignore
        }

        /* ───────── 2. figure out the salesman ───────── */
        Integer salesmanId = resolveSalesPerson(req);
        if (salesmanId == null) {
            req.setSalesPersonId(req.userId);
            return;
        } // nobody to notify – bail out

        req.setSalesPersonId(salesmanId);

        NotificationSetting set = db.getNotificationSetting(req.clientId);

        /* sensible fallbacks */
        int days = (set != null && set.daysBefore != null) ? set.daysBefore : 1;
        Integer nType = (set != null && set.notificationType != null) ? set.notificationType
                : NotificationType.WEB.getNotificationType();

        String subjTpl = (set != null && set.emailSubject != null) ? set.emailSubject
                : "Inspection due {{date}}";
        String msgTpl = (set != null && set.emailMessage != null) ? set.emailMessage
                : "The certificate “{{document}}” must be inspected on {{date}}.";

        LocalDateTime fireAt = next.minusDays(days).atTime(9, 0);

        /* token replacement */
        String subj = subjTpl.replace("{{date}}", next.toString())
                .replace("{{document}}", req.documentName);
        String body = msgTpl.replace("{{date}}", next.toString())
                .replace("{{document}}", req.documentName);

        Notification n = new Notification();
        n.notificationType = nType;
        n.notificationFrequency = 0;
        n.notificationSubject = subj;
        n.notificationMessage = body;
        n.scheduleTime = fireAt;
        n.recipientId = salesmanId;

        notifications.schedule(n, req.clientId, req.userId);
    }

    /* ---------- helpers ---------- */

    private String extract(ProcessRequest req, String key) {
        return req.documentType.documentFormat.searchTerms.stream()
                .filter(t -> t.searchTerm.equalsIgnoreCase(key))
                .map(SearchTerm::getSearchResult)
                .findFirst().orElse(null);
    }

    /** Implements the 3-step rule from the spec */
    private Integer resolveSalesPerson(ProcessRequest req) {

        User processor = db.findUserById(req.userId);

        /* a) processed by salesman himself? */
        if (processor.userRole == UserRole.SALESMAN.getRole()) {
            return processor.userId;
        }

        /* b) Admin processed – try job_number → Flask lookup */
        String jobNo = extract(req, "job_number");
        if (jobNo != null && !jobNo.equalsIgnoreCase("no data found")) {
            try {
                HttpClient cli = HttpClient.newHttpClient();
                HttpRequest httpReq = HttpRequest.newBuilder()
                        .uri(URI.create(SystemProperties.getProcessingUrl() + "/salesperson_by_job"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers
                                .ofString("{\"job_number\":\"" + jobNo + "\"}"))
                        .build();

                HttpResponse<String> resp = cli.send(httpReq, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 200) {
                    JsonNode json = new ObjectMapper().readTree(resp.body());

                    /* the Flask service returns *username*, possibly "john_smith" */
                    String raw = Optional.ofNullable(json.get("salesperson"))
                            .map(JsonNode::asText)
                            .orElse(null);

                    if (raw != null && !raw.equalsIgnoreCase("Not Assigned")) {

                        /* try a couple of normalisations before we give up */
                        String candidate1 = raw.trim(); // as-is
                        String candidate2 = raw.replace('_', ' ').trim(); // snake → spaces

                        User u = Optional.ofNullable(db.findUserByUserName(candidate1))
                                .orElse(db.findUserByUserName(candidate2));

                        if (u != null)
                            return u.userId; // success
                    }
                }
            } catch (Exception ignored) {
            }
        }
        /* c) fallback – notify the admin who processed */
        return processor.userId;
    }
}
