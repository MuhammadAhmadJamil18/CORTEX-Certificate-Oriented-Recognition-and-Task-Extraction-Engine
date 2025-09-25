package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.SearchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchResultRepository extends JpaRepository<SearchResult, Integer> {

    List<SearchResult> findByDocumentId(
            Integer clientId
    );
}
