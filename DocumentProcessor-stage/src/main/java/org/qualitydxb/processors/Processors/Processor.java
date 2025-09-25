package org.qualitydxb.processors.Processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.dal.Models.DocumentFormat;
import org.qualitydxb.dal.Models.DocumentType;
import org.qualitydxb.dal.Models.Notification;
import org.qualitydxb.dal.Models.SearchTerm;
import org.qualitydxb.dal.Service.DBService;
import org.qualitydxb.infrastructure.ApplicationContext;
import org.qualitydxb.notifications.Notifications;
import org.qualitydxb.processors.Process;
import org.qualitydxb.reminders.InspectionReminderService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.qualitydxb.infrastructure.SystemProperties.getProcessingUrl;

@Service
public abstract class Processor extends Process {

    public Processor() {}

    public static Processor getProcessor(String tag) {
        return getProcessor(Processor.class, tag);
    }

    /* ───────────────────────────── main entry ───────────────────────────── */
    public static ProcessRequest process(ProcessRequest request) throws Exception {

        DBService db   = ApplicationContext.getBean(DBService.class);
        Processor word = Processor.getProcessor("WORD");

        if (word == null) {
            return new ProcessRequest(ResponseCodes.PROCESSOR_NOT_FOUND,
                                      request.documentExtension, "");
        }

        /* 0. preload */
        request.parseDate = LocalDateTime.now();
        request.documentTypes =
                db.getDocumentTypeAndFormat(request.clientId, request.documentExtension);

        /* 1. classify via Flask */
        String text = word.getDocumentText(request.documentPath);
        String url  = getProcessingUrl() + "/classify_document";

        ObjectMapper om = new ObjectMapper();
        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString(om.writeValueAsString(Map.of("document_text", text))))
                .build();

        String detected = null;
        try {
            HttpResponse<String> resp = HttpClient.newHttpClient()
                    .send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonNode n = om.readTree(resp.body());
                detected = n.path("document_type").isNull()
                          ? null
                          : n.get("document_type").asText();
            }
        } catch (Exception ignore) { }

        /* 2. resolve DocumentType */
        final String detectedName = detected;     // ← effectively-final copy

        DocumentType dt = null;
        if (detectedName != null && !detectedName.isBlank()) {
            dt = request.documentTypes.stream()
                    .filter(d -> d.getDocumentTypeName()
                                  .equalsIgnoreCase(detectedName))
                    .findFirst()
                    .orElse(db.getDocumentTypeByName(request.clientId, detectedName));
        }
        if (dt == null) {                         // fallback → “other”
            dt = request.documentTypes.stream()
                    .filter(d -> d.getDocumentTypeName()
                                  .equalsIgnoreCase("other"))
                    .findFirst()
                    .orElse(request.documentTypes.getFirst());
        }

        /* 3. pick or fetch at least one format */
        if (dt.documentFormats == null || dt.documentFormats.isEmpty()) {
            List<DocumentFormat> all = db.getDocumentFormatsByType(dt.documentTypeId);
            if (all != null && !all.isEmpty()) dt.documentFormats = all;
        }
        DocumentFormat fmt = (dt.documentFormats != null && !dt.documentFormats.isEmpty())
                           ? dt.documentFormats.get(0)
                           : null;

        /* 4. load search-terms */
        if (fmt != null &&
            (fmt.searchTerms == null || fmt.searchTerms.isEmpty())) {
            fmt.searchTerms = db.getSearchTermsByFormatId(fmt.formatId, request.clientId);
        }

        /* 5. stamp IDs */
        request.documentFormatId = (fmt != null) ? fmt.formatId : null;
        request.documentTypeId   = dt.documentTypeId;
        request.documentType     = dt;
        request.documentType.documentFormat = fmt;

        /* 6. field extraction */
        request = word.processDocument(request);

        /* clean up heavy lists */
        request.documentTypes = null;
        if (request.documentType != null) request.documentType.documentFormats = null;

        /* 7. persist + notifications */
        try {
            ApplicationContext.getBean(InspectionReminderService.class).handle(request);

            request.documentId = db.saveDocumentResult(request);

            if (fmt != null && fmt.searchTerms != null) {
                fmt.searchTerms = db.saveSearchResult(request.documentId, fmt.searchTerms);
            }

            String msg = (fmt != null && fmt.searchTerms != null)
                       ? fmt.searchTerms.stream()
                             .map(t -> t.getName() + ": " + t.getValue())
                             .collect(Collectors.joining("\n"))
                       : "";

            Notification n = new Notification();
            n.notificationType    = NotificationType.WEB.getNotificationType();
            n.notificationSubject = "New document processed";
            n.notifyAll           = true;
            n.notificationMessage = msg;
            // ApplicationContext.getBean(Notifications.class)
            //                   .notify(n, request.clientId, request.userId);

        } catch (Exception e) {
            return new ProcessRequest(ResponseCodes.DATABASE_ERROR,
                                      request.documentExtension, e.getMessage());
        }
        return request;
    }

    /* ---------- abstract hooks ---------- */
    protected abstract ProcessRequest processDocument(ProcessRequest request) throws Exception;
    protected abstract List<SearchTerm> sendRequest(ProcessRequest r, String ep) throws Exception;
    protected abstract String getDocumentText(String path) throws IOException;
}
