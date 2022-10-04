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

package com.vmware.mangle.utils.clients.restclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.CertificateHelper;

/**
 * @author bkaranam (bhanukiran karanam)
 *
 *         Customized RestClient helper
 */
@Log4j2
@SuppressWarnings({ "squid:S3510", "squid:S1186" })
public class RestTemplateWrapper {
    private static final String POST_STARTS_MESSAGE =
            "*****************API POST STARTS*************************************";
    private static final String PUT_STARTS_MESSAGE =
            "*****************API PUT STARTS*************************************";
    private static final String DELETE_STARTS_MESSAGE =
            "*****************API DELETE STARTS*************************************";
    private static final String GET_STARTS_MESSAGE =
            "*****************API GET STARTS*************************************";
    private static final String API_ENDS_MESSAGE = "*****************API ENDS*************************************";
    private RestTemplate restTemplate;
    private ObjectMapper mapper;
    private HttpMethod method;
    private HttpHeaders headers;
    @SuppressWarnings("rawtypes")
    private HttpEntity entity;
    private String baseUrl;
    private String requestUrl;
    private String jsonRequest;
    private MultiValueMap<String, Object> formData;
    private String commonQueryParameter;

    private static final String RESPONSE_BODY = "Response body";

    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER =
            new TrustManager[] { CertificateHelper.getX509TrustManager() };

    public RestTemplateWrapper() {
        headers = new HttpHeaders();
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestErrorHandler());
        turnOffSslChecking();
        restTemplate.getMessageConverters().add(createXmlHttpMessageConverter());
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                try {
                    mapper = new ObjectMapperFactory().getObject();
                } catch (Exception e) {
                    log.error("Failed to create Jackson wrapper", e);
                }
                jsonConverter.setObjectMapper(mapper);
            }
        }
        log.trace("End of Create configure RestTemplate");
    }

    private void setAnnoationClass(Class<?> clz) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof MarshallingHttpMessageConverter) {
                XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();

                xStreamMarshaller.setAnnotatedClasses(clz);
                ((MarshallingHttpMessageConverter) converter).setMarshaller(xStreamMarshaller);
                ((MarshallingHttpMessageConverter) converter).setUnmarshaller(xStreamMarshaller);
                messageConverters.remove(converter);
                messageConverters.add(converter);
            }
        }
        restTemplate.setMessageConverters(messageConverters);
    }

    private HttpMessageConverter<Object> createXmlHttpMessageConverter() {
        MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xmlConverter.setMarshaller(xStreamMarshaller);
        xmlConverter.setUnmarshaller(xStreamMarshaller);
        return xmlConverter;
    }

    protected void turnOffSslChecking() {
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e);
        }
    }

    public ResponseEntity get(String urlSuffix, Class<?> clz) {
        log.trace(GET_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.GET);
        setRequestUrl(this.baseUrl + urlSuffix);
        return execute(clz);
    }

    public ResponseEntity get(String urlSuffix, ParameterizedTypeReference<?> responseType) {
        log.trace(GET_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.GET);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = null;
        return execute(responseType);
    }

    public ResponseEntity put(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.trace(PUT_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity put(String urlSuffix, String jsonRequest, ParameterizedTypeReference<?> responseType) {
        log.trace(PUT_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity post(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.trace(POST_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity post(String urlSuffix, String jsonRequest,
            ParameterizedTypeReference<?> responseType) {
        log.trace(POST_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity post(String urlSuffix, String jsonRequest, Class<?> clz, MediaType mediaType) {
        this.headers.setContentType(mediaType);
        return post(urlSuffix, jsonRequest, clz);
    }

    public ResponseEntity postForEntity(String urlSuffix, MultiValueMap<String, Object> formData, Class<?> clz) {
        return postForEntity(urlSuffix, formData, clz, MediaType.MULTIPART_FORM_DATA);
    }

    public ResponseEntity postForEntity(String urlSuffix, MultiValueMap<String, Object> formData, Class<?> clz,
            MediaType mediaType) {
        log.trace(POST_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.POST);
        this.headers.setContentType(mediaType);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
        this.formData = formData;
        return execute(clz);
    }

    public ResponseEntity postForEntity(String urlSuffix, MultiValueMap<String, Object> formData,
            ParameterizedTypeReference<?> responseType) {
        return postForEntity(urlSuffix, formData, responseType, MediaType.MULTIPART_FORM_DATA);
    }

    public ResponseEntity postForEntity(String urlSuffix, MultiValueMap<String, Object> formData,
            ParameterizedTypeReference<?> responseType, MediaType mediaType) {
        log.trace(POST_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.POST);
        this.headers.setContentType(mediaType);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.formData = formData;
        return execute(responseType);
    }

    public ResponseEntity delete(String urlSuffix, Class<?> clz) {
        return delete(urlSuffix, null, clz);
    }

    public ResponseEntity delete(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.trace(DELETE_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity delete(String urlSuffix, ParameterizedTypeReference<?> responseType) {
        return delete(urlSuffix, null, responseType);
    }

    public ResponseEntity delete(String urlSuffix, String jsonRequest, ParameterizedTypeReference<?> responseType) {
        log.trace(DELETE_STARTS_MESSAGE);
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    private ResponseEntity execute(Class<?> clz) {
        ResponseEntity response = null;
        try {
            intializeHttpEntity();
            setAnnoationClass(clz);
            response = restTemplate.exchange(getRequestUrl(), getHttpRequestMethod(), entity, clz);
            if (null != response.getBody()) {
                log.trace(RESPONSE_BODY + "-" + objectToJson(response.getBody()));
            }
            return response;
        } catch (RestClientException e) {
            return handleHttpServerErrorException(e);
        } finally {
            log.trace(API_ENDS_MESSAGE);
        }
    }

    private ResponseEntity execute(ParameterizedTypeReference<?> responseType) {
        ResponseEntity response = null;

        try {
            intializeHttpEntity();
            response = restTemplate.exchange(getRequestUrl(), getHttpRequestMethod(), entity, responseType);
            log.trace(RESPONSE_BODY + "-" + objectToJson(response.getBody()));
            return response;

        } catch (RestClientException e) {
            return handleHttpServerErrorException(e);
        } finally {
            log.trace(API_ENDS_MESSAGE);
        }
    }

    private void intializeHttpEntity() {
        log.trace("URL - " + getHttpRequestMethod().toString() + " - " + getRequestUrl());
        log.trace("Headers - " + headers.getContentType().toString());
        String mediaType = headers.getContentType().toString();
        if (mediaType.contains(MediaType.APPLICATION_JSON_VALUE) && jsonRequest != null && !"".equals(jsonRequest)) {
            entity = new HttpEntity<String>(jsonRequest, headers);
        } else if (mediaType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
        } else {
            entity = new HttpEntity<String>(headers);
        }
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity handleHttpServerErrorException(RestClientException e) {
        ResponseEntity response = null;
        if (e.toString().contains("HttpServerErrorException")) {

            String[] errorMsg = e.toString().split("HttpServerErrorException: ");
            String[] errorCode;
            if (errorMsg[0].contains(" ")) {
                errorCode = errorMsg[0].split(" ");
                response = new ResponseEntity(HttpStatus.valueOf(Integer.parseInt(errorCode[1])));
            } else {
                errorCode = errorMsg[1].split(" ");
                response = new ResponseEntity(HttpStatus.valueOf(Integer.parseInt(errorCode[0])));
            }
            log.trace(e);
        } else {
            log.trace(e);
        }
        return response;
    }

    public void setHttpRequestMethod(HttpMethod method) {
        this.method = method;

    }

    public HttpMethod getHttpRequestMethod() {
        return this.method;
    }

    public static String objectToJson(Object object) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new MangleRuntimeException(String.format("Failed to process Json : %s", object.toString()), e,
                    ErrorCode.GENERIC_ERROR);
        }
        return json;
    }

    public static String objectToGson(Object object) {
        String json;
        try {
            json = new Gson().toJson(object);
        } catch (Exception e) {
            throw new MangleRuntimeException(String.format("Failed to process Json : %s", object.toString()), e,
                    ErrorCode.GENERIC_ERROR);
        }
        return json;
    }

    public static <T> T jsonToObject(String json, Class<T> object) {
        try {
            return new ObjectMapper().readValue(json, object);
        } catch (JsonSyntaxException jse) {
            log.error("Input String is not a valid json" + jse.getMessage());
            return null;
        } catch (IOException e) {
            throw new MangleRuntimeException(e, ErrorCode.MALFORMED_PLUGIN_DESCRIPTOR);
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }

    private void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        if (!StringUtils.isEmpty(getCommonQueryParameter())) {
            this.requestUrl = requestUrl + commonQueryParameter;
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @SuppressWarnings("rawtypes")
    public HttpEntity getEntity() {
        return entity;
    }

    @SuppressWarnings("rawtypes")
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public void setCommonQueryParameter(String commonQueryParameter) {
        this.commonQueryParameter = commonQueryParameter;
    }

    public String getCommonQueryParameter() {
        return this.commonQueryParameter;
    }
}
