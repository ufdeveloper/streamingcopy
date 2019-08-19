package com.sardal.projects.helpers;

import com.sardal.projects.helpers.dto.InitiateUploadResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;

@Slf4j
@Data
@Component
@ConfigurationProperties("helpers.google")
public class GCSHelper {

    private String bucketName;
    private String uploadFileName;
    private String initiateResumableUploadBaseUrl;

    public String createResumableUploadUrl(String gcsAccessToken, String contentType, String contentLength) throws URISyntaxException {

        RestTemplate restTemplate = new RestTemplate();

        String initiateUploadUrl = initiateResumableUploadBaseUrl + "b/" + bucketName
                + "/o?uploadType=resumable&name=" + uploadFileName;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + gcsAccessToken);
        httpHeaders.add("X-Upload-Content-Type", contentType);
        httpHeaders.add("X-Upload-Content-Length", contentLength);

        HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<InitiateUploadResponseDTO> responseEntity;
        try {
            responseEntity = restTemplate.exchange(initiateUploadUrl, HttpMethod.POST, httpEntity, InitiateUploadResponseDTO.class);
        } catch (HttpClientErrorException e) {
            log.error("client error initiating upload to GCS, error={}", e);
            throw e;
        } catch (Exception e) {
            log.error("Error initiating upload to GCS, error={}", e);
            throw e;
        }

        log.info("Successfully initiated upload to GCS");
        HttpHeaders responseEntityHeaders = responseEntity.getHeaders();
        return responseEntityHeaders.getLocation().toString();
    }
}
