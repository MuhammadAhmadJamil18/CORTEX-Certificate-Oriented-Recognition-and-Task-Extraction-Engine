package org.qualitydxb.processors.Processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.dal.Models.DocumentType;
import org.qualitydxb.dal.Models.SearchTerm;
import org.qualitydxb.infrastructure.LoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.qualitydxb.infrastructure.SystemProperties.getProcessingUrl;

public class PdfProcessor extends Processor {

    private static final Logger logger = LoggerFactory.getLogger(Processor.class);
    protected String fileTag = "PDF";
    @Override
    protected String getFileTag() {
        return this.fileTag;
    }

    @Override
    protected ProcessRequest processDocument(ProcessRequest request) throws Exception {
        String endpoint = getProcessingUrl()+"/extract";
        request.documentType.documentFormat.searchTerms = sendRequest(request,endpoint);

        request.message= ResponseCodes.DOCUMENT_PROCESSED.getValue();
        request.messageCode= ResponseCodes.DOCUMENT_PROCESSED.getCode();

        return request;
    }

    @Override
    protected String getDocumentText(String documentPath) throws IOException {
        Path filePath = Paths.get(documentPath);
        try {
            PDDocument document = PDDocument.load(filePath.toFile());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text= pdfStripper.getText(document);
            document.close();
            return  text;
        } catch (IOException e) {
            throw new IOException();
        }
    }

    @Override
    protected List<SearchTerm> sendRequest(ProcessRequest request, String endpoint) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serializeRequest(request)))
                .build();

        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 500) {
                throw new Exception("Server error occurred while communicating with 'Processing' server.");
            }

            if (response.statusCode() >= 400) {
                throw new Exception("Client error occurred. HTTP Status: " + response.statusCode());
            }

            return deserializeResponse(request, response.body());
        } catch (IOException e) {
            throw new Exception("Failed to connect to the 'Processing' server.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Request to 'Processing' server was interrupted.", e);
        }
    }

    private String serializeRequest(ProcessRequest request) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> limitedRequest = null;
        if(request.isLink){
            limitedRequest=Map.of(
                    "fields", request.documentType.documentFormat.searchTerms.stream().map(SearchTerm::getSearchTerm).collect(Collectors.toList()),
                    "mega_link",  request.documentName
                    );
        } else{
            limitedRequest=Map.of(
                    "fields", request.documentType.documentFormat.searchTerms.stream().map(SearchTerm::getSearchTerm).collect(Collectors.toList()),
                    "document_text", getDocumentText(request.documentPath)
                    // "possible_document_types", request.documentTypes.stream().map(DocumentType::getDocumentTypeName).collect(Collectors.toList())
                    );
//             limitedRequest = Map.of(
//     "fields", List.of("client_name", "next_date_of_inspection", "job_number", "certificate_number", "date_of_inspection"),
//     "document_text", getDocumentText(request.documentPath)
// );

        }

        try {
            return objectMapper.writeValueAsString(limitedRequest);
        } catch (JsonProcessingException e) {
            throw new IOException("Error in serializing JSON.", e);
        } catch (Exception e) {
            throw new IOException("Unexpected error occurred while serializing the request.", e);
        }
    }

    private List<SearchTerm> deserializeResponse(ProcessRequest request, String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, String>>() {});
            List<SearchTerm> searchTerms = request.documentType.documentFormat.searchTerms;

            for (int i = 0; i < searchTerms.size(); i++) {
                SearchTerm term = searchTerms.get(i);
                String key = term.searchTerm;

                if (responseMap.containsKey(key)) {
                    term.searchResult=responseMap.get(key);
                }
            }

            return searchTerms;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }
}
