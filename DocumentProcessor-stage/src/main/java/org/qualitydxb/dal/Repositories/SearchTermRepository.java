package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.SearchTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchTermRepository extends JpaRepository<SearchTerm, Integer> {
    List<SearchTerm> findByDocumentTypeIdAndDocumentFormatId(Integer documentTypeId, Integer documentFormatId);

    SearchTerm findBySearchTermId(Integer searchTermId);

    List<SearchTerm> findByDocumentFormatIdAndClient(Integer documentFormatId, Integer clientId);

}
