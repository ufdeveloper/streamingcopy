package com.sardal.projects.controller;

import com.sardal.projects.helpers.AWSHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("streamingcopy/helpers")
public class HelperController {

    @Autowired
    private AWSHelper awsHelper;

    @RequestMapping(value = "getS3SignedUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String getS3SignedUrl() throws URISyntaxException {

        log.info("Received request to generate pre-signed url");
        String s3SignedUrl = awsHelper.getS3SignedUrl();
        log.info("Successfully retrieved s3SignedUrl={}", s3SignedUrl);
        return s3SignedUrl;
    }
}
