package org.qualitydxb.dal.Service;

import org.qualitydxb.common.Enums.NotificationStatus;
import org.qualitydxb.common.Enums.NotificationType;
import org.qualitydxb.common.Enums.UserRole;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.common.Models.ReportsRequest;
import org.qualitydxb.dal.Models.*;
import org.qualitydxb.dal.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class DBService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private DocumentFormatRepository documentFormatRepository;
    @Autowired
    private SearchTermRepository searchTermRepository;
    @Autowired
    private SearchResultRepository searchResultRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    public User login(String email) {
        return userRepository.findByUserEmail(email);
    }

    public Notification saveNotification(Notification notification) {
        notification.createdAt = LocalDateTime.now();
        return notificationRepository.save(notification);
    }

    public List<Notification> findScheduledNotificationsBetween(LocalDateTime now, LocalDateTime oneMinuteLater) {
        return notificationRepository.findByScheduleTimeBetweenAndStatus(
                now,
                oneMinuteLater,
                NotificationStatus.SCHEDULED.getNotificationStatus());
    }

    public List<User> getUsersByClientIdAndRole(Integer clientId, boolean notifyAll) {
        if (notifyAll) {
            return userRepository.findByClientId(clientId);
        } else {
            return userRepository.findByClientIdAndUserRole(clientId, 2);// 2 is salesman
        }
    }

    public List<DocumentType> getDocumentTypeAndFormat(int clientId, String fileExtension) {
        List<DocumentType> documentTypes = documentTypeRepository.findByClientId(clientId);

        for (DocumentType documentType : documentTypes) {
            documentType.documentFormats = documentFormatRepository
                    .findByDocumentTypeIdAndFileExtension(documentType.documentTypeId, fileExtension);
        }

        for (DocumentType documentType : documentTypes) {
            for (DocumentFormat documentFormat : documentType.documentFormats) {
                documentFormat.searchTerms = searchTermRepository
                        .findByDocumentTypeIdAndDocumentFormatId(documentType.documentTypeId, documentFormat.formatId);
            }
        }

        return documentTypes;
    }

    public Integer saveDocumentResult(ProcessRequest request) {
        return documentRepository.save(new Document(request)).documentId;
    }

    public List<SearchTerm> saveSearchResult(Integer documentId, List<SearchTerm> searchTerms) {
        for (SearchTerm searchTerm : searchTerms) {
            searchTerm.searchResultId = searchResultRepository.save(new SearchResult(documentId, searchTerm)).id;
        }
        return searchTerms;
    }

    public List<Notification> getAllNotifications(int clientId, int userid) {
        List<Notification> notifications = notificationRepository.findByClientIdAndUserId(clientId, userid);

        for (Notification notification : notifications) {
            notification.notifiedBy = userRepository.findByUserId(notification.userId).userName;

            // Log or debug the recipientId
            System.out.println("Recipient ID: " + notification.recipientId);

            // Check if recipientId is valid and log the user data
            if (notification.recipientId != null) {
                User recipient = userRepository.findByUserId(notification.recipientId);
                if (recipient != null) {
                    notification.setRecipientName(recipient.userName);
                } else {
                    System.out.println("No user found for recipientId: " + notification.recipientId);
                }
            }
        }

        return notifications;
    }

    public List<Notification> getAllNotifications(int clientId, int userid, int userRole,
            NotificationStatus notificationStatus) {
        List<Notification> notifications = null;

        if (userRole == UserRole.ADMIN.getRole()) {
            notifications = notificationRepository.findByClientIdAndUserIdAndNotifyAllAndStatus(clientId, userid, true,
                    notificationStatus.getNotificationStatus());
        } else {
            notifications = notificationRepository.findByClientIdAndNotifyAllAndStatus(clientId, true,
                    notificationStatus.getNotificationStatus());
        }

        for (Notification notification : notifications) {
            notification.notifiedBy = userRepository.findByUserId(notification.userId).userName;
        }

        return notifications;
    }

    // public ReportsRequest generateReport(ReportsRequest reportsRequest) {
    // LocalDate startDate = LocalDate.parse(reportsRequest.startDate);
    // LocalDate endDate = LocalDate.parse(reportsRequest.endDate);

    // LocalDateTime startDateTime = startDate.atStartOfDay(); // Default time 00:00
    // LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    // if (reportsRequest.documentType == 0) {
    // reportsRequest.report =
    // documentRepository.findByClientIdAndParseDateBetween(reportsRequest.clientId,
    // startDateTime, endDateTime);
    // } else if (reportsRequest.documentFormat == null) {
    // reportsRequest.report =
    // documentRepository.findByClientIdAndDocumentTypeIdAndParseDateBetween(reportsRequest.clientId,
    // reportsRequest.documentType, startDateTime, endDateTime);
    // } else {
    // reportsRequest.report =
    // documentRepository.findByClientIdAndDocumentTypeIdAndDocumentFormatIdAndParseDateBetween(reportsRequest.clientId,
    // reportsRequest.documentType, reportsRequest.documentFormat, startDateTime,
    // endDateTime);
    // }

    // for (Document document : reportsRequest.report) {
    // document.searchResultList =
    // searchResultRepository.findByDocumentId(document.documentId);
    // document.userName = userRepository.findByUserId(document.userId).userName;
    // document.salesPersonName=document.userName;

    // for (SearchResult searchResult : document.searchResultList) {
    // searchResult.searchKey =
    // searchTermRepository.findBySearchTermId(searchResult.searchTermId).searchTerm;
    // }
    // }

    // reportsRequest.report.removeIf(document ->
    // document.searchResultList.isEmpty());
    // return reportsRequest;
    // }

    public ReportsRequest generateReport(
            ReportsRequest req,
            int userId,
            int userRole) {
        LocalDate fromDate = LocalDate.parse(req.dateFrom, DateTimeFormatter.ISO_DATE);
        LocalDate toDate = LocalDate.parse(req.dateTo, DateTimeFormatter.ISO_DATE);

        int page = Optional.ofNullable(req.pageNumber).orElse(0);
        int size = Optional.ofNullable(req.pageSize).orElse(20);
        Pageable paging = PageRequest.of(page, size, Sort.by("parseDate").descending());

        Page<Document> pageResult;

        boolean isSales = (userRole == UserRole.SALESMAN.getRole());
        String cat = req.dateCategory;

        if ("processing".equals(cat)) {
            LocalDateTime startDt = fromDate.atStartOfDay();
            LocalDateTime endDt = toDate.atTime(23, 59, 59);
            if (req.documentType == 0) {
                pageResult = documentRepository.findByClientIdAndParseDateBetween(req.clientId, startDt, endDt, paging);
            } else if (req.documentFormat == null) {
                pageResult = documentRepository.findByClientIdAndDocumentTypeIdAndParseDateBetween(
                        req.clientId,
                        req.documentType,
                        startDt,
                        endDt,
                        paging);
            } else {
                pageResult = documentRepository.findByClientIdAndDocumentTypeIdAndDocumentFormatIdAndParseDateBetween(
                        req.clientId,
                        req.documentType,
                        req.documentFormat,
                        startDt,
                        endDt,
                        paging);
            }
        } else if ("inspection".equals(cat)) {
            String start = fromDate.toString();
            String end = toDate.toString();
            if (req.documentType == 0) {
                pageResult = documentRepository.findByInspectionDateBetween(req.clientId, start, end, paging);
            } else if (req.documentFormat == null) {
                pageResult = documentRepository.findByInspectionDateAndTypeBetween(
                        req.clientId,
                        req.documentType,
                        start,
                        end,
                        paging);
            } else {
                pageResult = documentRepository.findByInspectionDateAndTypeFormatBetween(
                        req.clientId,
                        req.documentType,
                        req.documentFormat,
                        start,
                        end,
                        paging);
            }
        } else {
            String start = fromDate.toString();
            String end = toDate.toString();
            if (req.documentType == 0) {
                pageResult = documentRepository.findByNextInspectionDateBetween(req.clientId, start, end, paging);
            } else if (req.documentFormat == null) {
                pageResult = documentRepository.findByNextInspectionDateAndTypeBetween(
                        req.clientId,
                        req.documentType,
                        start,
                        end,
                        paging);
            } else {
                pageResult = documentRepository.findByNextInspectionDateAndTypeFormatBetween(
                        req.clientId,
                        req.documentType,
                        req.documentFormat,
                        start,
                        end,
                        paging);
            }
        }

        List<Document> docs = new ArrayList<>(pageResult.getContent());

        if (isSales) {
            docs.removeIf(d -> !(d.userId.equals(userId) ||
                    (d.salesPersonId != null && d.salesPersonId.equals(userId))));
        }

        for (Document d : docs) {
            List<SearchResult> list = searchResultRepository.findByDocumentId(d.documentId);
            for (SearchResult sr : list) {
                var term = searchTermRepository.findBySearchTermId(sr.searchTermId);
                sr.searchKey = term != null ? term.getSearchTerm() : null;
            }
            d.searchResultList = list;

            d.userName = userRepository.findByUserId(d.userId).userName;
            if (d.salesPersonId != null) {
                var sp = userRepository.findByUserId(d.salesPersonId);
                d.salesPersonName = sp != null ? sp.userName : "N/A";
            } else {
                d.salesPersonName = "N/A";
            }
        }

        if (!"processing".equals(cat)) {
            docs.removeIf(d -> d.searchResultList.isEmpty());
        }

        req.report = docs;
        req.pageNumber = pageResult.getNumber();
        req.pageSize = pageResult.getSize();
        req.totalPages = pageResult.getTotalPages();
        req.totalRecords = pageResult.getTotalElements();
        return req;
    }

    public List<DocumentType> getReportFilters(int clientId) {
        List<DocumentType> documentTypes = documentTypeRepository.findByClientId(clientId);

        for (DocumentType documentType : documentTypes) {
            documentType.documentFormats = documentFormatRepository.findByDocumentTypeId(documentType.documentTypeId);
        }

        return documentTypes;
    }

    public List<User> getAllUsers(int clientId) {
        List<User> user = userRepository.findByClientId(clientId);
        for (User u : user) {
            u.userPassword = "";
        }
        return user;
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<Integer> getProcessingStatistics(int clientId, int userId, int userRole) {
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            LocalDateTime startDateTime = LocalDate.of(LocalDate.now().getYear(), i, 1).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.of(LocalDate.now().getYear(), i, 1).plusMonths(1).minusDays(1)
                    .atTime(23, 59, 59);

            if (userRole == UserRole.ADMIN.getRole()) {
                values.add(documentRepository.findByClientIdAndParseDateBetween(clientId, startDateTime, endDateTime)
                        .size());
            } else {
                values.add(documentRepository
                        .findByClientIdAndUserIdAndParseDateBetween(clientId, userId, startDateTime, endDateTime)
                        .size());
            }
        }

        return values;
    }

    public Document getDocumentById(Integer documentId) {
        return documentRepository
                .findById(documentId)
                .orElse(null);
    }

    public User findUserById(Integer id) {
        return userRepository.findByUserId(id);
    }

    public User findUserByUserName(String userName) {
        return userRepository.findByUserNameIgnoreCase(userName);
    }

    public List<Notification> getRelevantNotifications(int clientId,
            int userId,
            int userRole) {

        if (userRole == UserRole.ADMIN.getRole()) {
            List<Notification> notifications = null;
            notifications = notificationRepository.findByClientId(clientId);
            for (Notification notification : notifications) {
                notification.notifiedBy = userRepository.findByUserId(notification.userId).userName;

                // Set recipientName only for admins
                if (notification.recipientId != null) {
                    User recipient = userRepository.findByUserId(notification.recipientId);
                    if (recipient != null) {
                        notification.setRecipientName(recipient.userName);
                    } else {
                        System.out.println("No user found for recipientId: " + notification.recipientId);
                    }
                } else {
                    notification.setRecipientName("Unknown Recipient");
                }
            }
            return notifications;
        }

        return notificationRepository.findForSalesman(
                clientId,
                userId,
                NotificationStatus.DELIVERED.getNotificationStatus());
    }

    public DocumentType getDocumentTypeByName(Integer clientId, String name) {
        return documentTypeRepository
                .findFirstByClientIdAndDocumentTypeNameIgnoreCase(clientId, name)
                .orElse(null);
    }

    public List<DocumentFormat> getDocumentFormatsByType(Integer documentTypeId) {
        return documentFormatRepository.findByDocumentTypeId(documentTypeId);
    }

    public List<SearchTerm> getSearchTermsByFormatId(Integer formatId, Integer clientId) {
        return searchTermRepository.findByDocumentFormatIdAndClient(formatId, clientId);
    }

    public NotificationSetting getNotificationSetting(int clientId) {
        return notificationSettingRepository.findFirstByClientId(clientId);
    }

    public NotificationSetting saveNotificationSetting(NotificationSetting ns) {
        return notificationSettingRepository.save(ns);
    }

}