package com.sardal.projects.service;

import java.io.IOException;

public interface StreamingCopyService {

    /**
     *
     * @param sourceUrl
     * @param targetUrl
     */
    void copy(String sourceContentLength, String sourceUrl, String targetUrl) throws IOException;

}
