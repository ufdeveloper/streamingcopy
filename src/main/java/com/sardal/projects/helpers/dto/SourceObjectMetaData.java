package com.sardal.projects.helpers.dto;

import lombok.Data;

/**
 * Created by shantanu on 7/30/19.
 */

@Data
public class SourceObjectMetaData {
    String signedUrl;
    Long contentLength;
    String contentType;
}
