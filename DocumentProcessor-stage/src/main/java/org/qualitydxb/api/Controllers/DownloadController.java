package org.qualitydxb.api.Controllers;

import org.apache.commons.io.FilenameUtils;
import org.qualitydxb.dal.Models.Document;
import org.qualitydxb.dal.Service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/processor")
public class DownloadController {

    @Autowired
    private DBService dbService;

    // must match your upload folder
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    /**
     * Download the exact file the user uploaded.
     * Exposes the Content-Disposition header so the browser can pick up the original filename.
     */
    @CrossOrigin(
      origins = "*",  // or restrict to your frontend origin(s)
      exposedHeaders = HttpHeaders.CONTENT_DISPOSITION
    )
    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Integer documentId,
            @RequestAttribute(name = "clientId", required = true) Integer clientId,
            @RequestAttribute(name = "userId",   required = true) Integer userId
    ) {
        // 1) fetch metadata
        Document doc = dbService.getDocumentById(documentId);
        if (doc == null || !doc.clientId.equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }

        // 2) determine original filename pieces
        String originalName = doc.documentName;                             // e.g. "report.docx"
        String baseName     = FilenameUtils.getBaseName(originalName);      // "report"
        String ext          = FilenameUtils.getExtension(originalName);     // "docx"

        // 3) find the actual file on disk (handles "report.docx" or "report(1).docx", etc)
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Path found      = null;
        String pattern  = Pattern.quote(baseName) + "\\(\\d+\\)\\." + Pattern.quote(ext);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath, baseName + "*." + ext)) {
            for (Path p : stream) {
                String fn = p.getFileName().toString();
                if (fn.equals(originalName) || fn.matches(pattern)) {
                    found = p;
                    break;
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error reading upload directory", e
            );
        }

        if (found == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Uploaded file not found on disk"
            );
        }

        // 4) wrap in Resource
        Resource resource;
        try {
            resource = new UrlResource(found.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invalid file URL", e
            );
        }

        // 5) detect MIME type
        MediaType contentType;
        try {
            String probe = Files.probeContentType(found);
            contentType = (probe != null)
                        ? MediaType.parseMediaType(probe)
                        : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // 6) build a proper Content-Disposition header with UTF-8 support
        ContentDisposition cd = ContentDisposition
                .attachment()
                .filename(originalName, StandardCharsets.UTF_8)
                .build();

        // 7) return the response
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(resource);
    }
}
