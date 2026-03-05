package com.example.CargoAssign.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ImageKitService {

    private static final String IMAGEKIT_UPLOAD_URL =
            "https://upload.imagekit.io/api/v1/files/upload";

    @Value("${imagekit.private-key}")
    private String privateKey;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is required");
        }

        if (privateKey == null || privateKey.isBlank()) {
            throw new IOException("Image upload service is not configured");
        }
        if (privateKey.startsWith("dummy-")) {
            throw new IOException("IMAGEKIT_PRIVATE_KEY is not configured");
        }

        String originalName = file.getOriginalFilename();
        String safeFileName = (originalName == null || originalName.isBlank())
                ? "upload-file"
                : originalName;

        String auth = Base64.getEncoder()
                .encodeToString((privateKey + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return safeFileName;
            }
        });

        body.add("fileName", safeFileName);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(
                    IMAGEKIT_UPLOAD_URL,
                    request,
                    Map.class
            );
        } catch (RestClientException ex) {
            throw new IOException("Image upload failed at provider");
        }

        Map responseBody = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful()
                || responseBody == null
                || responseBody.get("url") == null) {
            throw new IOException("Image upload failed");
        }

        return responseBody.get("url").toString();
    }
}
