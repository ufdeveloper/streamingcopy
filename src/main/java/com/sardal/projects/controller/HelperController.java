package com.sardal.projects.controller;

import com.sardal.projects.helpers.AWSHelper;
import com.sardal.projects.helpers.GCSHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("streamingcopy/helpers")
public class HelperController {

    @Autowired
    private AWSHelper awsHelper;

    @Autowired
    private GCSHelper gcsHelper;

    @RequestMapping(value = "aws/createS3SignedUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String getS3SignedUrl() throws URISyntaxException {
        log.info("Received request to generate pre-signed url");
        String s3SignedUrl = awsHelper.createS3SignedUrl();
        log.info("Successfully retrieved s3SignedUrl={}", s3SignedUrl);
        return s3SignedUrl;
    }

    @RequestMapping(value = "gcs/createResumableUploadUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String getGCSResumableUploadUrl(HttpServletRequest request) throws URISyntaxException {
        log.info("Received request to generate resumable upload url");
        String resumableUploadUrl = gcsHelper.createResumableUploadUrl(getGCSAccessToken(request));
        log.info("Successfully generated resumableUploadUrl={}", resumableUploadUrl);
        return resumableUploadUrl;
    }

    private String getGCSAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
