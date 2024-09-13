package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private static CrptApi instance;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String uri = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private final long requestLimit;
    private long requestCount;

    private CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        startTimer(timeUnit.toMillis(1));
    }

    public static CrptApi getInstance() {
        if (instance == null) {
            instance = new CrptApi(TimeUnit.SECONDS, 5);
        }
        return instance;
    }

    public synchronized HttpResponse<String> createDocument(Document document, String signature) throws IOException, InterruptedException {
        while (requestCount++ >= requestLimit) {
            Thread.sleep(100);
        }
        return client.send(createRequest(document, signature), HttpResponse.BodyHandlers.ofString());
    }

    private void startTimer(long millis) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                requestCount = 0;
            }
        }, 0, millis);
    }

    private HttpRequest createRequest(Document document, String signature) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(document);

        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    @Getter
    @Setter
    static class Document {
        private Description description;
        private String docId;
        private String docStatus;
        private DocType docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private Date productionDate;
        private String productionType;
        private List<Product> products;
        private Date regDate;
        private String regNumber;

        @Getter
        @Setter
        class Description {
            public String participantInn;
        }

        enum DocType {
            LP_INTRODUCE_GOODS
        }
    }

    @Getter
    @Setter
    static class Product {
        private String certificateDocument;
        private Date certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private Date productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi crptApi = CrptApi.getInstance();
        HttpResponse<String> response = crptApi.createDocument(new Document(), "signature");
    }
}