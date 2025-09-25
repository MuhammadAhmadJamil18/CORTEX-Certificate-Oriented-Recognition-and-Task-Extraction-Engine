package org.qualitydxb.api.Controllers;

import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.infrastructure.LoggerService;
import org.qualitydxb.infrastructure.SystemProperties;
import org.qualitydxb.processors.Processors.Processor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.qualitydxb.infrastructure.MegaDownloadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Map;


@RestController
@RequestMapping("/api/processor")
public class ProcessorController {




    private final ExecutorService executorService = Executors.newFixedThreadPool(SystemProperties.getMaxConcurrentFiles());

    @PostMapping(value = "/process", consumes = {"multipart/form-data"})
    public ResponseEntity<ProcessRequest> process( @RequestParam("document") MultipartFile document, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) throws IOException {
       try {
            // System.out.println("document received");
            return ResponseEntity.ok(Processor.process(new ProcessRequest(document, clientId, userId)));
       } catch (Exception ex) {
            LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
            return ResponseEntity.status(401).body(new ProcessRequest(ResponseCodes.SERVER_ERROR,"", ex.getMessage()));
       }
    }

    @PostMapping(value = "/processLinkk")
    public ResponseEntity<ProcessRequest> processLink(@RequestParam("documentsLink") String documentsLink, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try {
            return ResponseEntity.ok(Processor.process(new ProcessRequest(documentsLink, clientId,userId)));
        } catch (Exception ex) {
            LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
            return ResponseEntity.status(401).body(new ProcessRequest(ResponseCodes.SERVER_ERROR,"", ex.getMessage()));
        }
    }

    @PostMapping("/processLinkkk")
    public ResponseEntity<?> processLinkkk(
        @RequestBody Map<String, Object> requestBody,
        @RequestAttribute(name = "clientId", required = true) Integer clientId,
        @RequestAttribute(name = "userId", required = true) Integer userId
    ) {
        try {
            // Convert the request body to a JSON string (existing logic)
            ObjectMapper mapper = new ObjectMapper();
            String jsonRequest = mapper.writeValueAsString(requestBody);
            System.out.println("jsonRequest: " + jsonRequest);

            // Extract the mega_link from the JSON
            String megaLink = (String) requestBody.get("mega_link");
            if (megaLink != null && !megaLink.trim().isEmpty()) {
                // Create an instance of MegaDownloadService and perform the download
                MegaDownloadService megaDownloadService = new MegaDownloadService();
                // Define the destination directory (adjust as needed)
                String destinationDirectory = "src/main/resources/uploads";
                System.out.println("Downloading file from MEGA link: " + megaLink);
                megaDownloadService.downloadFile(megaLink, destinationDirectory);
            } else {
                System.out.println("No mega_link found in the request.");
            }

            // Prepare HTTP call to the Flask service using the same JSON request
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
            String flaskUrl = "http://127.0.0.1:5000/extract";

            // POST the JSON to Flask and return its response
            ResponseEntity<String> flaskResponse = restTemplate.postForEntity(flaskUrl, entity, String.class);
            return ResponseEntity
                   .status(flaskResponse.getStatusCode())
                   .body(flaskResponse.getBody());

        } catch (Exception ex) {
            LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
            return ResponseEntity
                   .status(401)
                   .body(new ProcessRequest(ResponseCodes.SERVER_ERROR, "", ex.getMessage()));
        }
    }

    @PostMapping("/processLink")
public ResponseEntity<ProcessRequest> processLink(
    @RequestBody Map<String, Object> requestBody,
    @RequestAttribute(name = "clientId", required = true) Integer clientId,
    @RequestAttribute(name = "userId", required = true) Integer userId
) {
    try {
        // Extract the "mega_link" field from the incoming JSON
        String megaLink = (String) requestBody.get("mega_link");
        if (megaLink == null || megaLink.trim().isEmpty()) {
            throw new IllegalArgumentException("No mega_link provided in the request");
        }
        // Create the ProcessRequest; the constructor downloads the file into the same upload folder
        ProcessRequest processRequest = new ProcessRequest(megaLink, clientId, userId);
        // Process the downloaded file using the same logic as the file upload
        ProcessRequest result = Processor.process(processRequest);
        return ResponseEntity.ok(result);
    } catch (Exception ex) {
        LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
        return ResponseEntity.status(401)
                .body(new ProcessRequest(ResponseCodes.SERVER_ERROR, "", ex.getMessage()));
    }
}


    @PostMapping(value = "/processAll")
    public ResponseEntity<List<ProcessRequest>> processAll(@RequestParam("files") MultipartFile[] files, @RequestAttribute(name = "clientId", required = true) Integer clientId, @RequestAttribute(name = "userId", required = true) Integer userId) {
        try {
            if(files.length > SystemProperties.getMaxConcurrentFiles()) {
                return ResponseEntity.ok(Collections.singletonList(new ProcessRequest(ResponseCodes.MAX_CONCURRENT_FILES_EXCEEDED,"", "")));
            }

            List<CompletableFuture<ProcessRequest>> futures = new ArrayList<>();
            for (MultipartFile file : files) {
                CompletableFuture<ProcessRequest> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        ProcessRequest processRequest = new ProcessRequest(file, clientId, userId);
                        return Processor.process(processRequest);
                    } catch (Exception ex) {
                        LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
                        return new ProcessRequest(ResponseCodes.SERVER_ERROR,"", ex.getMessage());
                    }
                }, executorService);
                futures.add(future);
            }

            List<ProcessRequest> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            LoggerService.log(ex, Project.PROCESSOR, LogTag.ERROR);
            return ResponseEntity.status(401).body(Collections.singletonList(new ProcessRequest(ResponseCodes.MAX_CONCURRENT_FILES_EXCEEDED,"", ex.getMessage())));
        }
    }
}


