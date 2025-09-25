package org.qualitydxb.common.Models;

import org.apache.commons.io.FilenameUtils;
import org.qualitydxb.common.Enums.DocumentSource;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.dal.Models.DocumentType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.qualitydxb.infrastructure.MegaDownloadService;

public class ProcessRequest {

    private final String uploadPath = "src/main/resources/uploads/";

    public Integer clientId;
    public Integer userId;
    public Integer documentSource;
    public boolean isLink;

    public Integer documentId;
    public String documentPath;
    public String documentName;
    public String documentExtension;

    public DocumentType documentType;
    public List<DocumentType> documentTypes;

    public String message;
    public Integer messageCode;
    public String exception;
    public Integer documentFormatId;
    public Integer documentTypeId;
    public LocalDateTime parseDate;

    private Integer salesPersonId;
    public Integer getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(Integer salesPersonId) { this.salesPersonId = salesPersonId; }

    // 2) (Optional) convenience ctors
    public ProcessRequest(MultipartFile document, Integer clientId, Integer userId, Integer salesPersonId) throws IOException {
        this(document, clientId, userId);
        this.salesPersonId = salesPersonId;
    }

    public ProcessRequest(String documentsLink, Integer clientId, Integer userId, Integer salesPersonId) throws IOException,InterruptedException  {
        this(documentsLink, clientId, userId);
        this.salesPersonId = salesPersonId;
    }

    public ProcessRequest(){}

    public ProcessRequest(MultipartFile file, Integer clientId, Integer userId) throws IOException {
        this.clientId=clientId;
        this.userId=userId;
        this.documentSource= DocumentSource.UPLOAD.getSource();
        this.documentExtension= FilenameUtils.getExtension(file.getOriginalFilename());
        this.documentName=file.getOriginalFilename();
        saveFile(file);
    }

    public ProcessRequest(ResponseCodes response, String documentExtension, String exception){
        this.documentExtension=documentExtension;
        this.message=response.getValue()+documentExtension;
        this.messageCode=response.getCode();
        this.exception=exception;
    }

    public ProcessRequest(String documentsLink, Integer clientId, Integer userId) throws IOException, InterruptedException {
        this.clientId = clientId;
        this.userId = userId;
        this.documentSource = DocumentSource.LINK.getSource();
        this.isLink = true;
        downloadFile(documentsLink);
    }

    private void saveFile(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get(uploadPath + file.getOriginalFilename());
        Files.write(path, bytes);
        this.documentPath = path.toString();
    }

    private void downloadFile(String link) throws IOException, InterruptedException {
        MegaDownloadService megaDownloadService = new MegaDownloadService();
        String destinationDirectory = this.uploadPath; 
        // Download the file; it will be saved with its inherent filename
        megaDownloadService.downloadFile(link, destinationDirectory);
        
        // Wait briefly to ensure the download is completed (adjust the duration as needed)
        Thread.sleep(2000);
        
        // List files in the destination directory and select the most recently modified file
        java.io.File dir = new java.io.File(destinationDirectory);
        java.io.File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new IOException("Downloaded file not found in destination directory");
        }
        
        java.io.File latestFile = files[0];
        for (java.io.File file : files) {
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
        }
        
        // Set the document name and path based on the downloaded file
        this.documentName = latestFile.getName();
        this.documentExtension = FilenameUtils.getExtension(latestFile.getName());
        this.documentPath=(uploadPath + latestFile.getName()).toString();
    }


    public void deleteFile() {
        if (this.documentPath != null && !this.documentPath.isEmpty()) {
            java.io.File file = new java.io.File(this.documentPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
}
