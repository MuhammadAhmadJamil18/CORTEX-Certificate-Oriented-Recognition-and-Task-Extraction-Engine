package org.qualitydxb.reports;

import org.qualitydxb.common.Models.ReportsRequest;
import org.qualitydxb.dal.Models.DocumentFormat;
import org.qualitydxb.dal.Models.DocumentType;
import org.qualitydxb.dal.Service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class Reports {

    @Autowired
    DBService dbService;

    public ReportsRequest fetch(ReportsRequest reportsRequest, int clientId, int userId, int userRole) {
        reportsRequest.clientId=clientId;
        reportsRequest.userId=userId;

        return dbService.generateReport(reportsRequest, userId, userRole);
    }

    public List<DocumentType> filters(int clientId) {
        return dbService.getReportFilters(clientId);
    }

    public ReportsRequest statistics(int clientId, int userId, int userRole) {
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December");
        List<Integer> values = dbService.getProcessingStatistics(clientId, userId, userRole);

        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.statistics=Map.of("months", months, "values", values);

        return reportsRequest;
    }
}
