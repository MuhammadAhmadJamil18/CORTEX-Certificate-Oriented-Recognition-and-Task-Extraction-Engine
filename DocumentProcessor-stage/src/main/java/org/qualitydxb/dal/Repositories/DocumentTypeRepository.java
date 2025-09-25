package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {
    List<DocumentType> findByClientId(Integer clientid);
    Optional<DocumentType> findFirstByClientIdAndDocumentTypeNameIgnoreCase(Integer clientId,
                                                                        String documentTypeName);

}
