package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.DocumentFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentFormatRepository extends JpaRepository<DocumentFormat, Integer> {
    List<DocumentFormat> findByDocumentTypeIdAndFileExtension(int documentTypeId, String fileExtension);

    List<DocumentFormat> findByDocumentTypeId(int documentTypeId);
}
