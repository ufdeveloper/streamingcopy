package com.sardal.projects.service;

import com.sardal.projects.exceptions.InternalServerErrorException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

@Slf4j
@Data
@Service
public class StreamingCopyServiceImpl implements StreamingCopyService {

    private static final Long GCS_UPLOAD_CHUNK_SIZE = 150*1024*1024L; // 150MB
    private static final Long S3_DOWNLOAD_BUFFER_SIZE = 1024*1024L;   // 1MB

    RestTemplate restTemplate = new RestTemplate();

    @Override
    public void copy(String sourceContentLength, String sourceUrl, String targetUrl) throws IOException {

        // Get InputStream from pre-signed url
        InputStream inputStream = null;
        ReadableByteChannel inputChannel = null;
        try {
            inputStream = URI.create(sourceUrl).toURL().openConnection().getInputStream();
            inputChannel = Channels.newChannel(inputStream);
        } catch (Exception e) {
            log.error("Error reading from pre-signed url, exception={}", e);
            inputChannel.close();
            inputStream.close();
            throw new InternalServerErrorException("Error reading from pre-signed url" + e);
        }

        log.info("Successfully fetched inputStream from sourceUrl");

        uploadFileInChunks(targetUrl, inputChannel, Long.valueOf(sourceContentLength));

        log.info("Successfully copied file");

        inputChannel.close();
        inputStream.close();
    }

    public void uploadFileInChunks(String resumableUploadUrl, ReadableByteChannel s3InputChannel, long fileLengthInBytes) {

        int totalBytesRead = 0;
        int rangeEndResponse = -1;

        int chunkLength = (int) Math.min(fileLengthInBytes, GCS_UPLOAD_CHUNK_SIZE);
        ByteBuffer chunk = ByteBuffer.allocate(chunkLength);

        try {
            while (totalBytesRead < fileLengthInBytes) {

                getChunkFromInputStream(chunk, s3InputChannel, chunkLength);
                int rangeStart = rangeEndResponse + 1;
                int rangeEnd = rangeStart + chunkLength - 1;
                log.info("Content-Length:" + chunkLength);
                log.info("bytes " + rangeStart + "-" + rangeEnd + "/" + fileLengthInBytes);
                rangeEndResponse = uploadFileChunk(resumableUploadUrl, chunk.array(), chunkLength, rangeStart, rangeEnd, fileLengthInBytes);
                totalBytesRead += chunkLength;

                // cleanup and setup for next loop
                chunk.clear();
                chunkLength = (int) Math.min(fileLengthInBytes-totalBytesRead, GCS_UPLOAD_CHUNK_SIZE);
            }
        } catch (IOException e) {
            log.error("Error reading from s3InputStream", e);
            throw new InternalServerErrorException("Error reading from s3InputStream" + e);
        }
    }

    protected void getChunkFromInputStream(ByteBuffer chunk, ReadableByteChannel inputChannel, int chunkLength) throws IOException{
        log.info("getChunkFromInputStream, chunkLength={}", chunkLength);
        int totalBytesRead = 0;
        int s3bufferSize = (int) Math.min(chunkLength, S3_DOWNLOAD_BUFFER_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(s3bufferSize);
        try {
            while (totalBytesRead < chunkLength) {
                inputChannel.read(buffer);
                buffer.flip(); // Prepare the buffer to be drained
                chunk.put(buffer);
                totalBytesRead += buffer.position();
                buffer.clear(); // Empty buffer to get ready for filling
            }
        } catch (Exception e) {
            log.error("error reading from inputChannel, totalBytesRead={}, error={}", totalBytesRead, e);
            throw e;
        }
    }

    protected int uploadFileChunk(String resumableUploadUrl, byte[] buffer, int chunkLength, int rangeStart, int rangeEnd, long fileLengthInBytes) {

        byte[] payload;

        if(buffer.length > chunkLength) {
            payload = Arrays.copyOfRange(buffer, 0, chunkLength);
        } else {
            payload = buffer;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Length", String.valueOf(payload.length));
        httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLengthInBytes);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(payload, httpHeaders);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(resumableUploadUrl, HttpMethod.PUT, httpEntity, String.class);
        } catch (RestClientException e) {
            if(e instanceof HttpClientErrorException) {
                log.error("Client error uploading chunk to GCS", e);
                throw e;
            }
            log.error("Server error uploading chunk to GCS", e);
            throw new InternalServerErrorException("Server error uploading chunk to GCS" + e);
        }

        if(responseEntity.getStatusCode() == HttpStatus.PERMANENT_REDIRECT) {
            return (int) responseEntity.getHeaders().getRange().get(0).getRangeEnd(fileLengthInBytes);
        } else if(responseEntity.getStatusCode() == HttpStatus.OK) {
            return -1;
        } else {
            log.error("Incorrect response status code from GCS chunk upload");
            throw new InternalServerErrorException("Incorrect response status code from GCS chunk upload, statusCode=" + responseEntity.getStatusCode());
        }
    }
}
