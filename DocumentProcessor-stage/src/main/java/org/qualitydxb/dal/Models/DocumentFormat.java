package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import org.qualitydxb.common.Enums.ResponseCodes;

import java.util.List;

@Entity
@Table(name = "documentformat", schema = "qualitydxb")
public class DocumentFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formatid")
    public Integer formatId; // Primary Key

    @Column(name = "formatname")
    public String formatName; // Format Name

    @Column(name = "fileextension")
    public String fileExtension; // File Extension

    @Column(name="documenttypeid")
    public Integer documentTypeId; // Document Type ID

    @Transient
    public List<SearchTerm> searchTerms;

    @Transient
    public String message;
    @Transient
    public int messageCode;

    public DocumentFormat() {
    }

    public DocumentFormat(ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
    }
}
