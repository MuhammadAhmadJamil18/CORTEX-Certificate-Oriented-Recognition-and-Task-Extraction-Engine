package org.qualitydxb.common.Models;

import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.Document;
import java.util.List;
import java.util.Map;

public class ReportsRequest {

    public int clientId;
    public int userId;

    public int documentType;
    public Integer documentFormat;
    public String startDate;
    public String endDate;

    public Integer pageNumber = 0;
    public Integer pageSize   = 20;
    public Integer totalPages;
    public Long    totalRecords;

    public String dateCategory; // "processing" | "inspection" | "nextInspection"
    public String dateFrom;     // ISO "YYYY-MM-DD"
    public String dateTo;       // ISO "YYYY-MM-DD"
    

    public List<Document> report;

    public Map<String, Object> statistics;

    public int messageCode;
    public String message;

    public ReportsRequest(){}

    public ReportsRequest(ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
    }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }
}
