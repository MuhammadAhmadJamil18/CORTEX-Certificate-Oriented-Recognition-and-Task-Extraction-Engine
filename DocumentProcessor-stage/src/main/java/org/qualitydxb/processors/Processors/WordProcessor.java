package org.qualitydxb.processors.Processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.qualitydxb.common.Enums.ResponseCodes;
import org.qualitydxb.common.Models.ProcessRequest;
import org.qualitydxb.dal.Models.SearchTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.qualitydxb.infrastructure.SystemProperties.getProcessingUrl;

public class WordProcessor extends Processor {

    private static final Logger log = LoggerFactory.getLogger(Processor.class);
    private final String fileTag = "WORD";

    @Override protected String getFileTag() { return fileTag; }

    /* ───────── main doc-level workflow ───────── */
    @Override
    protected ProcessRequest processDocument(ProcessRequest req) throws Exception {

        String endpoint = getProcessingUrl() + "/extract";
        List<SearchTerm> results = sendRequest(req, endpoint);
        if (req.documentType.documentFormat != null)
            req.documentType.documentFormat.searchTerms = results;

        req.message     = ResponseCodes.DOCUMENT_PROCESSED.getValue();
        req.messageCode = ResponseCodes.DOCUMENT_PROCESSED.getCode();

        if (req.isLink) req.deleteFile();
        return req;
    }

    /* ───────── Word text extraction ───────── */
    @Override
    protected String getDocumentText(String path) throws IOException {

        Path p = Paths.get(path);
        String name = p.toString().toLowerCase();

        try {
            if (name.endsWith(".docx")) {
                try (FileInputStream in = new FileInputStream(path);
                     XWPFDocument doc  = new XWPFDocument(in);
                     XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                    return ex.getText();
                }
            } else if (name.endsWith(".doc")) {
                try (FileInputStream in = new FileInputStream(path);
                     HWPFDocument doc  = new HWPFDocument(in);
                     WordExtractor ex  = new WordExtractor(doc)) {
                    return ex.getText();
                }
            } else {
                throw new IllegalArgumentException("Unsupported file: " + path);
            }
        } catch (IOException e) {
            log.error("Error reading Word document", e);
            throw new IOException("Failed to read Word document", e);
        }
    }

    /* ───────── HTTP call to Flask /extract ───────── */
    @Override
    protected List<SearchTerm> sendRequest(ProcessRequest req, String endpoint) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        String body = serializeRequest(req);

        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp =
                client.send(httpReq, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() >= 500)
            throw new Exception("Processing-server error " + resp.statusCode());
        if (resp.statusCode() >= 400)
            throw new Exception("Client error " + resp.statusCode());

        return deserializeResponse(req, resp.body());
    }

    /* ───────── helpers ───────── */
    private String serializeRequest(ProcessRequest req) throws IOException {

        ObjectMapper om = new ObjectMapper();

        List<String> fields =
            (req.documentType.documentFormat != null &&
             req.documentType.documentFormat.searchTerms != null)
           ? req.documentType.documentFormat.searchTerms
                 .stream().map(SearchTerm::getSearchTerm).collect(Collectors.toList())
           : List.of();                                    // empty list safe-guard

        Map<String,Object> payload = Map.of(
                "fields",        fields,
                "document_text", getDocumentText(req.documentPath)
        );
        try {
            return om.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IOException("JSON serialisation failed", e);
        }
    }

    private List<SearchTerm> deserializeResponse(ProcessRequest req, String body) {

        ObjectMapper om = new ObjectMapper();
        try {
            Map<String,String> map =
                om.readValue(body, new TypeReference<Map<String,String>>() {});

            if (req.documentType.documentFormat == null ||
                req.documentType.documentFormat.searchTerms == null)
                return List.of();                           // nothing to fill

            for (SearchTerm t : req.documentType.documentFormat.searchTerms) {
                if (map.containsKey(t.searchTerm))
                    t.searchResult = map.get(t.searchTerm);
            }
            return req.documentType.documentFormat.searchTerms;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse extractor JSON", e);
        }
    }
}
