/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.utils.exceptions.handler;

import java.util.Date;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.UnavailableException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.MangleTaskException;

/**
 * Generate the error response, if any Exception happened during execution.
 *
 * @author kumargautam
 */
@ControllerAdvice
@RestController
@Log4j2
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private CustomErrorMessage customErrorMessage;
    private static final String ERROR_CODE_MSG = "Error Code : ";
    private static final String ERROR_DESC_MSG = ", Error Message : ";
    private static final String CAUSE = ", cause : ";

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("", ex);
        String errorMessage = ex.getMessage();
        if (!StringUtils.hasText(errorMessage)) {
            errorMessage = ErrorConstants.INTERNAL_SERVER_ERROR;
        }
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ErrorCode.UNKNOWN_ERROR.getCode(), errorMessage,
                request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("", ex);
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(ErrorConstants.HTTP_METHOD_ERROR);
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));
        builder.setLength(builder.length() - 1);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                builder.toString(), request.getDescription(false));

        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("", ex);
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType().toString());
        builder.append(ErrorConstants.MEDIA_TYPE_ERROR);
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));
        builder.setLength(builder.length() - 1);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                builder.toString(), request.getDescription(false));
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MangleException.class)
    public final ResponseEntity<ErrorDetails> handleMangleException(MangleException exception, WebRequest request) {
        String customMessage =
                exception.getMessage() == null ? customErrorMessage.getErrorMessage(exception) : exception.getMessage();
        log.error(ERROR_CODE_MSG + exception.getErrorCode().getCode() + ERROR_DESC_MSG + customMessage,
                ((customMessage != null && customMessage.contains(ErrorConstants.NO_RECORD_FOUND_MSG)) ? null
                        : exception));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getErrorCode().getCode(), customMessage,
                request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MangleTaskException.class)
    public final ResponseEntity<ErrorDetails> handleMangleTaskException(MangleTaskException taskException,
            WebRequest request) {
        String customMessage = taskException.getMessage() == null ? customErrorMessage.getErrorMessage(taskException)
                : taskException.getMessage();
        log.error(ERROR_CODE_MSG + taskException.getErrorCode().getCode() + ERROR_DESC_MSG + customMessage,
                taskException);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), taskException.getErrorCode().getCode(),
                customErrorMessage.getErrorMessage(taskException), request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MangleRuntimeException.class)
    public final ResponseEntity<ErrorDetails> handleMangleRuntimeException(MangleRuntimeException exception,
            WebRequest request) {
        String errorDescription = customErrorMessage.getErrorMessage(exception);
        String customMessage = exception.getMessage() == null ? errorDescription : exception.getMessage();
        log.error(ERROR_CODE_MSG + exception.getErrorCode().getCode() + ERROR_DESC_MSG + customMessage,
                ((customMessage != null && customMessage.contains(ErrorConstants.NO_RECORD_FOUND_MSG)) ? null
                        : exception));

        errorDescription = exception.getMessage() == null ? errorDescription
                : String.format("%s Reason: %s", errorDescription, exception.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getErrorCode().getCode(), errorDescription,
                request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.NO_RECORD_FOUND);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        log.error("", ex);
        String errorCode = ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getCode();
        String errorMsg = customErrorMessage.getErrorMessage(errorCode);
        errorMsg = customErrorMessage.formatErrorMessage(errorMsg, ex.getName(), ex.getValue(),
                ex.getRequiredType().getSimpleName());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorCode, errorMsg, request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        log.error("", ex);
        String errorCode = ErrorCode.BAD_REQUEST.getCode();
        String errorMsg = customErrorMessage.getErrorMessage(errorCode) + CAUSE + ex.getLocalizedMessage();
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorCode, errorMsg, request.getDescription(false));
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        log.error("", ex);
        String errorCode = ErrorCode.BAD_REQUEST.getCode();
        String errorMsg =
                customErrorMessage.getErrorMessage(errorCode) + CAUSE + formBindExceptionMessage(ex.getBindingResult());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorCode, errorMsg, request.getDescription(false));
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.BAD_REQUEST);
    }

    private String formBindExceptionMessage(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        bindingResult.getFieldErrors().stream()
                .forEach(f -> builder.append(f.getField()).append(": ").append(f.getDefaultMessage()).append(","));
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        log.error(ex.getMessage());
        String errorCode = ErrorCode.BAD_REQUEST.getCode();
        String errorMsg =
                customErrorMessage.getErrorMessage(errorCode) + CAUSE + formBindExceptionMessage(ex.getBindingResult());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorCode, errorMsg, request.getDescription(false));
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(DataAccessException.class)
    public final ResponseEntity<ErrorDetails> handleCassandraException(DataAccessException exception,
            WebRequest request) {
        String errorMsg = exception.getMessage();
        log.error(errorMsg);
        if (exception.getCause() instanceof NoHostAvailableException) {
            NoHostAvailableException hostAvailableException = (NoHostAvailableException) exception.getCause();
            errorMsg = hostAvailableException.getCustomMessage(Integer.MAX_VALUE, true, false);
        } else if (exception.getCause() instanceof InvalidQueryException) {
            errorMsg = ErrorConstants.INVALID_QUERY_EXCEPTION;
        } else if (exception.getCause() instanceof UnavailableException) {
            errorMsg = ErrorConstants.UNAVAILABLE_EXCEPTION;
        }
        ErrorDetails errorDetails =
                new ErrorDetails(new Date(), ErrorCode.DB_ERROR.getCode(), errorMsg, request.getDescription(false));
        HttpHeaders headers = new HttpHeaders();
        headers.add(ErrorConstants.REQUEST_FAILED_MESSAGE_HEADER, ErrorConstants.ERROR_MSG);
        return new ResponseEntity<>(errorDetails, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}