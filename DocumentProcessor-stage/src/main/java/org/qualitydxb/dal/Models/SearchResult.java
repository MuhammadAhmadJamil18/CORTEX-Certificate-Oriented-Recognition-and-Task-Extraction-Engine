package org.qualitydxb.dal.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "searchresult", schema = "qualitydxb")
public class SearchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id; // Primary Key

    @Column(name = "documentid", nullable = false)
    public Integer documentId; // Foreign Key as Integer

    @Column(name = "searchtermid", nullable = false)
    public Integer searchTermId; // Foreign Key as Integer

    @Column(name = "searchresult", nullable = false)
    public String searchResult; // Search Term

    @Column(name="isdateortime", nullable = false)
    public Boolean isDateOrTime; // Boolean

    @Transient
    public String searchKey; // Search Term

    public SearchResult(){}

    public SearchResult(int documentId, SearchTerm searchTerm) {
        this.documentId = documentId;
        this.searchTermId = searchTerm.searchTermId;
        this.searchResult = searchTerm.searchResult;
        this.isDateOrTime=searchTerm.isDateOrTime;
    }
}
