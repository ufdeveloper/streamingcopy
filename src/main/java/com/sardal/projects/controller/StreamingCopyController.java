package com.sardal.projects.controller;

import com.sardal.projects.service.StreamingCopyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/streamingcopy")
public class StreamingCopyController {

    private static final String SOURCE_CONTENT_LENGTH = "contentLength";
    private static final String SOURCE_URL = "sourceUrl";
    private static final String TARGET_URL = "targetUrl";

    @Autowired
    private StreamingCopyService streamingCopyService;

    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public void copy(@RequestBody Map<String, String> requestMap) throws IOException {
        validateRequest(requestMap);
        log.info("Received request for streaming copy, sourceUrr={}, targetUrl={}",
                requestMap.get(SOURCE_URL), requestMap.get(TARGET_URL));
        streamingCopyService.copy(requestMap.get(SOURCE_CONTENT_LENGTH), requestMap.get(SOURCE_URL),
                requestMap.get(TARGET_URL));
    }

    private void validateRequest(Map<String, String> requestMap) {
        Assert.isTrue(!CollectionUtils.isEmpty(requestMap), "requestMap cannot be empty/null");
        Assert.isTrue(!StringUtils.isEmpty(requestMap.get(SOURCE_CONTENT_LENGTH)), "source object " +
                "contentLength cannot be empty/null");
        Assert.isTrue(!StringUtils.isEmpty(requestMap.get(SOURCE_URL)), "sourceUrl cannot be empty/null");
        Assert.isTrue(!StringUtils.isEmpty(requestMap.get(TARGET_URL)), "targetUrl cannot be empty/null");
    }


}
