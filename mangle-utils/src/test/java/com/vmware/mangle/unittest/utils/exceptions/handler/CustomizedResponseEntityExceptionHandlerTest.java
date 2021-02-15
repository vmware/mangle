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

package com.vmware.mangle.unittest.utils.exceptions.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.UnavailableException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.MangleTaskException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;
import com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test for CustomizedResponseEntityExceptionHandler.
 *
 * @author kumargautam
 */
public class CustomizedResponseEntityExceptionHandlerTest extends PowerMockTestCase {

    @InjectMocks
    private CustomizedResponseEntityExceptionHandler customizedResponseEntityExceptionHandler;
    @Mock
    private CustomErrorMessage customErrorMessage;
    @Mock
    private WebRequest request;
    private HttpHeaders headers;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.headers = new HttpHeaders();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        this.customErrorMessage = null;
        this.request = null;
        this.customizedResponseEntityExceptionHandler = null;
        this.headers = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void tearUp() {
        when(request.getDescription(anyBoolean())).thenReturn("/test");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleAllExceptions(java.lang.Exception, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleAllExceptions() {
        Exception exception = mock(Exception.class);
        when(exception.getMessage()).thenReturn("testHandleAllExceptions");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleAllExceptions(exception, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 500);
        verify(exception, times(2)).getMessage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleHttpRequestMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleHttpRequestMethodNotSupported() {
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getMessage()).thenReturn("testHandleHttpRequestMethodNotSupported");
        when(exception.getMethod()).thenReturn(HttpMethod.POST.name());
        Set<HttpMethod> methodSet = new HashSet<>();
        methodSet.add(HttpMethod.GET);
        when(exception.getSupportedHttpMethods()).thenReturn(methodSet);
        ResponseEntity<Object> actualRsult = customizedResponseEntityExceptionHandler
                .handleHttpRequestMethodNotSupported(exception, headers, HttpStatus.METHOD_NOT_ALLOWED, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 405);
        verify(exception, times(1)).getMessage();
        verify(exception, times(1)).getMethod();
        verify(exception, times(1)).getSupportedHttpMethods();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleHttpMediaTypeNotSupported(org.springframework.web.HttpMediaTypeNotSupportedException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleHttpMediaTypeNotSupported() {
        HttpMediaTypeNotSupportedException exception = mock(HttpMediaTypeNotSupportedException.class);
        when(exception.getMessage()).thenReturn("testHandleHttpRequestMethodNotSupported");
        when(exception.getContentType()).thenReturn(MediaType.APPLICATION_XML);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        mediaTypeList.add(MediaType.APPLICATION_JSON_UTF8);
        when(exception.getSupportedMediaTypes()).thenReturn(mediaTypeList);
        ResponseEntity<Object> actualRsult = customizedResponseEntityExceptionHandler
                .handleHttpMediaTypeNotSupported(exception, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 415);
        verify(exception, times(1)).getMessage();
        verify(exception, times(1)).getContentType();
        verify(exception, times(1)).getSupportedMediaTypes();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleMangleException(com.vmware.mangle.MangleException.MangleException, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleMangleException() {
        MangleException exception = mock(MangleException.class);
        when(exception.getErrorCode()).thenReturn(ErrorCode.GENERIC_ERROR);
        when(exception.getMessage()).thenReturn("testHandleMangleException");
        when(customErrorMessage.getErrorMessage(any(MangleException.class))).thenReturn("test HandleMangleException");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleMangleException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(exception, times(3)).getMessage();
        verify(exception, times(2)).getErrorCode();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleMangleTaskException(com.vmware.mangle.MangleTaskException.MangleTaskException, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleMangleTaskException() {
        MangleTaskException exception = mock(MangleTaskException.class);
        when(exception.getErrorCode()).thenReturn(ErrorCode.GENERIC_ERROR);
        when(exception.getMessage()).thenReturn("testHandleMangleTaskException");
        when(customErrorMessage.getErrorMessage(any(MangleTaskException.class)))
                .thenReturn("test HandleMangleTaskException");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleMangleTaskException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(exception, times(3)).getMessage();
        verify(exception, times(2)).getErrorCode();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleMangleRuntimeException(com.vmware.mangle.MangleRuntimeException.MangleRuntimeException, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleMangleRuntimeException() {
        MangleRuntimeException exception = mock(MangleRuntimeException.class);
        when(exception.getErrorCode()).thenReturn(ErrorCode.GENERIC_ERROR);
        when(exception.getMessage()).thenReturn("testHandleMangleRuntimeException");
        when(customErrorMessage.getErrorMessage(any(MangleRuntimeException.class)))
                .thenReturn("testHandleMangleRuntimeException");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleMangleRuntimeException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(exception, times(5)).getMessage();
        verify(exception, times(2)).getErrorCode();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleMethodArgumentTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException, org.springframework.web.context.request.WebRequest)}.
     *
     * @throws ClassNotFoundException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testHandleMethodArgumentTypeMismatch() throws ClassNotFoundException {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("value");
        when(exception.getValue()).thenReturn(4);
        Class class1 = Class.forName("java.lang.String");
        when(exception.getRequiredType()).thenReturn(class1);
        when(exception.getLocalizedMessage()).thenReturn("testHandleMethodArgumentTypeMismatch");
        when(customErrorMessage.getErrorMessage(anyString())).thenReturn("test HandleMethodArgumentTypeMismatch");
        when(customErrorMessage.formatErrorMessage(anyString(), anyString(), anyObject(), anyString()))
                .thenReturn("Field value should not be 4");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleMethodArgumentTypeMismatch(exception, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 400);
        verify(exception, times(1)).getName();
        verify(exception, times(1)).getValue();
        verify(customErrorMessage, times(1)).getErrorMessage(anyString());
        verify(customErrorMessage, times(1)).formatErrorMessage(anyString(), anyString(), anyObject(), anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getLocalizedMessage()).thenReturn("testHandleHttpMessageNotReadable");
        when(customErrorMessage.getErrorMessage(anyString())).thenReturn("test HandleHttpMessageNotReadable");
        ResponseEntity<Object> actualRsult = customizedResponseEntityExceptionHandler
                .handleHttpMessageNotReadable(exception, headers, HttpStatus.BAD_REQUEST, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 400);
        verify(exception, times(1)).getLocalizedMessage();
        verify(customErrorMessage, times(1)).getErrorMessage(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleBindException(org.springframework.validation.BindException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleBindException() {
        BindingResult bindingResult = mock(BindingResult.class);
        BindException exception = spy(new BindException(bindingResult));
        when(customErrorMessage.getErrorMessage(anyString())).thenReturn("test HandleBindException");
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("name");
        when(fieldError.getDefaultMessage()).thenReturn("must not be null");
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        ResponseEntity<Object> actualRsult = customizedResponseEntityExceptionHandler.handleBindException(exception,
                headers, HttpStatus.BAD_REQUEST, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 400);
        verify(customErrorMessage, times(1)).getErrorMessage(anyString());
        verify(bindingResult, times(1)).getFieldErrors();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
     */
    @Test
    public void testHandleMethodArgumentNotValid() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.getParameterIndex()).thenReturn(1);
        Executable executable = mock(Executable.class);
        when(methodParameter.getExecutable()).thenReturn(executable);
        MethodArgumentNotValidException exception =
                spy(new MethodArgumentNotValidException(methodParameter, bindingResult));
        when(customErrorMessage.getErrorMessage(anyString())).thenReturn("test HandleMethodArgumentNotValid");
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("name");
        when(fieldError.getDefaultMessage()).thenReturn("must not be null");
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        ResponseEntity<Object> actualRsult = customizedResponseEntityExceptionHandler
                .handleMethodArgumentNotValid(exception, headers, HttpStatus.PRECONDITION_FAILED, request);
        Assert.assertEquals(actualRsult.getStatusCodeValue(), 412);
        verify(customErrorMessage, times(1)).getErrorMessage(anyString());
        verify(bindingResult, times(1)).getFieldErrors();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleCassandraException(DataAccessException, WebRequest)}.
     */
    @Test
    public void testHandleCassandraExceptionForConnectionFailure() {
        DataAccessException exception = mock(DataAccessException.class);
        when(exception.getMessage()).thenReturn("testHandleCassandraExceptionForConnectionFailure");
        NoHostAvailableException noHostAvailableException = mock(NoHostAvailableException.class);
        when(exception.getCause()).thenReturn(noHostAvailableException);
        when(noHostAvailableException.getCustomMessage(anyInt(), anyBoolean(), anyBoolean()))
                .thenReturn("All host(s) tried for query failed");
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleCassandraException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        verify(exception, times(1)).getMessage();
        verify(exception, times(2)).getCause();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleCassandraException(DataAccessException, WebRequest)}.
     */
    @Test
    public void testHandleCassandraExceptionForInvalidQuery() {
        DataAccessException exception = mock(DataAccessException.class);
        when(exception.getMessage()).thenReturn("testHandleCassandraExceptionForInvalidQuery");
        InvalidQueryException invalidQueryException = mock(InvalidQueryException.class);
        when(exception.getCause()).thenReturn(invalidQueryException);
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleCassandraException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        verify(exception, times(1)).getMessage();
        verify(exception, times(2)).getCause();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.CustomizedResponseEntityExceptionHandler#handleCassandraException(DataAccessException, WebRequest)}.
     */
    @Test
    public void testHandleCassandraExceptionForUnavailableException() {
        DataAccessException exception = mock(DataAccessException.class);
        when(exception.getMessage()).thenReturn("testHandleCassandraExceptionForUnavailableException");
        UnavailableException dbException = mock(UnavailableException.class);
        when(exception.getCause()).thenReturn(dbException);
        ResponseEntity<ErrorDetails> actualRsult =
                customizedResponseEntityExceptionHandler.handleCassandraException(exception, request);
        Assert.assertEquals(actualRsult.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        verify(exception, times(1)).getMessage();
        verify(exception, times(3)).getCause();
    }
}
