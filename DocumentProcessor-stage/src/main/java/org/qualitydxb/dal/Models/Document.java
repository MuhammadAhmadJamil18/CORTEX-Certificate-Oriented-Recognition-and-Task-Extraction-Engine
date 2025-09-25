package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import org.qualitydxb.common.Models.ProcessRequest;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document", schema = "qualitydxb")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentid")
    public Integer documentId; // Primary Key

    @Column(name = "clientid")
    public Integer clientId; // Foreign Key

    @Column(name = "userid")
    public Integer userId; // Foreign Key

    @Column(name = "documentname")
    public String documentName; // Document Name

    @Column(name = "documentpath")
    public String documentPath; // Document Path

    @Column(name = "documentsource")
    public Integer documentSource; // Foreign Key

    @Column(name = "documentformatid")
    public Integer documentFormatId; // Foreign Key

    @Column(name = "documenttypeid")
    public Integer documentTypeId; // Foreign Key

    @Column(name = "parsedate")
    public LocalDateTime parseDate; // Parse Date

    @Column(name = "salespersonid")
    public Integer salesPersonId;

    @Transient
    public List<SearchResult> searchResultList;
    @Transient
    public String userName;
    @Transient
    public String salesPersonName;

    public Document(ProcessRequest request){
        this.clientId=request.clientId;
        this.userId=request.userId;
        this.documentName=request.documentName;
        this.documentPath=request.documentPath;
        this.documentSource=request.documentSource;
        this.documentTypeId=request.documentTypeId;
        this.documentFormatId=request.documentFormatId;
        this.parseDate=request.parseDate;
        this.salesPersonId = request.getSalesPersonId();

    }

    public Document() {

    }
}
