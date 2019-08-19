package com.sardal.projects.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Exception to be used when an unexpected error occured.
 *
 * Created by shantanu on 8/8/16.
 */
@ResponseStatus(INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends HttpStatusCodeException {

    public InternalServerErrorException(String msg) {
        super(INTERNAL_SERVER_ERROR, msg);
    }
}
