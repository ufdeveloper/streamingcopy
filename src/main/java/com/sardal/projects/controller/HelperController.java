package com.sardal.projects.controller;

import com.sardal.projects.helpers.AWSHelper;
import com.sardal.projects.helpers.GCSHelper;
import com.sardal.projects.helpers.dto.SourceObjectMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("streamingcopy/helpers")
public class HelperController {

    private static final String CONTENT_TYPE = "contentType";
    private static final String CONTENT_LENGTH = "contentLength";

    @Autowired
    private AWSHelper awsHelper;

    @Autowired
    private GCSHelper gcsHelper;

    @RequestMapping(value = "aws/createS3SignedUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public SourceObjectMetaData createS3SignedUrl() throws URISyntaxException {
        log.info("Received request to generate pre-signed url");
        SourceObjectMetaData sourceObjectMetaData = awsHelper.createS3SignedUrl();
        log.info("Successfully retrieved s3 sourceObjectMetadata={}", sourceObjectMetaData);
        return sourceObjectMetaData;
    }

    @RequestMapping(value = "gcs/createResumableUploadUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String createGCSResumableUploadUrl(HttpServletRequest request,
                                           @RequestBody Map<String, String> requestMap) throws URISyntaxException {
        log.info("Received request to generate resumable upload url, requestMap={}", requestMap);
        String resumableUploadUrl = gcsHelper.createResumableUploadUrl(getGCSAccessToken(request),
                requestMap.get(CONTENT_TYPE), requestMap.get(CONTENT_LENGTH));
        log.info("Successfully generated resumableUploadUrl={}", resumableUploadUrl);
        return resumableUploadUrl;
    }

    private String getGCSAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
