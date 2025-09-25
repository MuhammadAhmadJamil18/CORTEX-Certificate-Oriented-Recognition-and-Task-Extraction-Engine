package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "searchterm", schema = "qualitydxb")
public class SearchTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "searchtermid", nullable = false)
    public Integer searchTermId;

    @Column(name = "clientid", nullable = false)
    public Integer client;

    @Column(name = "documenttypeid", nullable = false)
    public Integer documentTypeId;

    @Column(name = "documentformatid", nullable = false)
    public Integer documentFormatId;

    @Getter
    @Column(name = "searchterm", nullable = false)
    public String searchTerm;

    @Column(name="isdateortime", nullable = false)
    public Boolean isDateOrTime; // Boolean

    @Transient
    public String searchResult;

    @Transient
    public Integer searchResultId;

    public String getName() {
        return this.searchTerm;
    }

    public String getValue() {
        return this.searchResult;
    }

    public String getSearchResult() {     
        return searchResult;
    }
}
