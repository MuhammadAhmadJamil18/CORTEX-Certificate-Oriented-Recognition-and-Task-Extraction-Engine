package org.qualitydxb.dal.Repositories;

import org.springframework.data.repository.query.Param;
import org.qualitydxb.dal.Models.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByClientIdAndDocumentTypeIdAndDocumentFormatIdAndParseDateBetween(
            Integer clientId,
            Integer documentType,
            Integer documentFormat,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Document> findByClientIdAndParseDateBetween(
            Integer clientId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Document> findByClientIdAndDocumentTypeIdAndParseDateBetween(
            Integer clientId,
            Integer documentTypeId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Document> findByClientIdAndUserIdAndParseDateBetween(
            Integer clientId,
            Integer userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    Page<Document> findByClientIdAndParseDateBetween(
            Integer clientId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Document> findByClientIdAndDocumentTypeIdAndParseDateBetween(
            Integer clientId,
            Integer documentTypeId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Document> findByClientIdAndDocumentTypeIdAndDocumentFormatIdAndParseDateBetween(
            Integer clientId,
            Integer documentTypeId,
            Integer formatId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
      SELECT d
        FROM Document d
       WHERE d.clientId   = :clientId
         AND d.parseDate BETWEEN :start AND :end
         AND (d.userId = :userId OR d.salesPersonId = :userId)
    """)
    Page<Document> findForSalesmanByDateRange(
      @Param("clientId") Integer clientId,
      @Param("start")    LocalDateTime start,
      @Param("end")      LocalDateTime end,
      @Param("userId")   Integer userId,
      Pageable page
    );

    @Query("""
      SELECT d
        FROM Document d
       WHERE d.clientId       = :clientId
         AND d.documentTypeId = :typeId
         AND d.parseDate     BETWEEN :start AND :end
         AND (d.userId = :userId OR d.salesPersonId = :userId)
    """)
    Page<Document> findForSalesmanByTypeAndDate(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer documentTypeId,
      @Param("start")    LocalDateTime start,
      @Param("end")      LocalDateTime end,
      @Param("userId")   Integer userId,
      Pageable page
    );

    @Query("""
      SELECT d
        FROM Document d
       WHERE d.clientId          = :clientId
         AND d.documentTypeId    = :typeId
         AND d.documentFormatId  = :fmtId
         AND d.parseDate        BETWEEN :start AND :end
         AND (d.userId = :userId OR d.salesPersonId = :userId)
    """)
    Page<Document> findForSalesmanByTypeFormatAndDate(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer documentTypeId,
      @Param("fmtId")    Integer documentFormatId,
      @Param("start")    LocalDateTime start,
      @Param("end")      LocalDateTime end,
      @Param("userId")   Integer userId,
      Pageable page
    );

    List<Document> findByClientId(Integer clientId);
    List<Document> findByClientIdAndDocumentTypeId(Integer clientId, Integer documentTypeId);
    List<Document> findByClientIdAndDocumentTypeIdAndDocumentFormatId(Integer clientId, Integer documentTypeId, Integer documentFormatId);

     @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId     = :clientId
         AND st.searchTerm  = 'date_of_inspection'
         AND sr.searchResult BETWEEN :start AND :end
    """)
    Page<Document> findByInspectionDateBetween(
      @Param("clientId") Integer clientId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );

    @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId        = :clientId
         AND d.documentTypeId  = :typeId
         AND st.searchTerm     = 'date_of_inspection'
         AND sr.searchResult  BETWEEN :start AND :end
    """)
    Page<Document> findByInspectionDateAndTypeBetween(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer typeId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );

    @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId         = :clientId
         AND d.documentTypeId   = :typeId
         AND d.documentFormatId = :fmtId
         AND st.searchTerm      = 'date_of_inspection'
         AND sr.searchResult   BETWEEN :start AND :end
    """)
    Page<Document> findByInspectionDateAndTypeFormatBetween(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer typeId,
      @Param("fmtId")    Integer fmtId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );

    // ─── filter on next_date_of_inspection ───
    @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId     = :clientId
         AND st.searchTerm  = 'next_date_of_inspection'
         AND sr.searchResult BETWEEN :start AND :end
    """)
    Page<Document> findByNextInspectionDateBetween(
      @Param("clientId") Integer clientId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );

    @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId        = :clientId
         AND d.documentTypeId  = :typeId
         AND st.searchTerm     = 'next_date_of_inspection'
         AND sr.searchResult  BETWEEN :start AND :end
    """)
    Page<Document> findByNextInspectionDateAndTypeBetween(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer typeId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );

    @Query("""
      SELECT DISTINCT d
        FROM Document d
        JOIN SearchResult sr ON sr.documentId = d.documentId
        JOIN SearchTerm st    ON st.searchTermId  = sr.searchTermId
       WHERE d.clientId         = :clientId
         AND d.documentTypeId   = :typeId
         AND d.documentFormatId = :fmtId
         AND st.searchTerm      = 'next_date_of_inspection'
         AND sr.searchResult   BETWEEN :start AND :end
    """)
    Page<Document> findByNextInspectionDateAndTypeFormatBetween(
      @Param("clientId") Integer clientId,
      @Param("typeId")   Integer typeId,
      @Param("fmtId")    Integer fmtId,
      @Param("start")    String start,
      @Param("end")      String end,
      Pageable page
    );
}