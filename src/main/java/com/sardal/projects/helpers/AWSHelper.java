package com.sardal.projects.helpers;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Data
@Component
@ConfigurationProperties("helpers.aws")
public class AWSHelper {

    private String s3BucketName;
    private String s3FilePath;
    private String s3ExpirationInSeconds;

    private AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

    public String getS3SignedUrl() throws URISyntaxException {

        Date s3UrlExpirationDate = Date.from(Instant.now().plusSeconds(Long.valueOf(s3ExpirationInSeconds)));
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3BucketName, s3FilePath);
        request.setExpiration(s3UrlExpirationDate);
        URL signedUrl = amazonS3Client.generatePresignedUrl(request);
        log.info("Successfully generated pre-signed url from s3, signedUrl={}", signedUrl);
        return signedUrl.toString();
    }
}
