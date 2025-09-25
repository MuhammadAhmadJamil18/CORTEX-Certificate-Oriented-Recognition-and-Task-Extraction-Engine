package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import lombok.Getter;
import org.qualitydxb.common.Enums.ResponseCodes;

import java.util.List;

@Entity
@Table(name = "documenttype", schema = "qualitydxb")
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documenttypeid")
    public Integer documentTypeId; // Primary Key

    @Getter
    @Column(name = "documenttypename")
    public String documentTypeName; // Document Type Name

    @Column(name = "description")
    public String description; // Description

    @Column(name = "clientid")
    public Integer clientId; // Client ID

    @Transient
    public DocumentFormat documentFormat;
    @Transient
    public List<DocumentFormat> documentFormats;

    @Transient
    public String message;
    @Transient
    public int messageCode;

    public DocumentType() {
    }

    public DocumentType(ResponseCodes response){
        this.message=response.getValue();
        this.messageCode=response.getCode();
    }
}
