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

package com.vmware.mangle.utils.restclienttemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author mangle
 *
 */
@Log4j2
public class RestTemplateWrapper {

    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }
    } };

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return hostname.equalsIgnoreCase(session.getPeerHost());
            }
        });
    }

    // set baseUrl and headers for must
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

    public RestTemplateWrapper() {

        headers = new HttpHeaders();
        restTemplate = new RestTemplate();
        // to handle 500 http error from server, the client has to set the error
        // handler
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
        log.debug("End of Create configure RestTemplate");
    }

    public static void turnOffSslChecking() {
        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.equalsIgnoreCase(session.getPeerHost());
                }
            });

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e);
        }
    }

    public static String objectToJson(Object object) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to process Json : %s", object.toString()), e);
        }
        return json;
    }

    public static <T> T jsonToObject(String json, Class<T> object) {
        try {
            new JsonParser().parse(json);
            return new ObjectMapper().readValue(json, object);
        } catch (JsonSyntaxException jse) {
            log.error("Input String is not a valid json" + jse.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to process Json : %s", json), e);
        }
    }

    public static <T> T yamlToObject(String yamlData, Class<T> object) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlData, object);
    }

    public static boolean objectToYamlFile(Object data, String filePath) {
        Yaml yaml = new Yaml();
        Writer output;
        try {
            output = new FileWriter(filePath);
            yaml.dump(data, output);
            return true;
        } catch (IOException e) {
            log.error("Dumping object to yaml file failed with exception:" + e.getMessage());
            return false;
        }
    }

    public static String objectToYaml(Object data) {
        Yaml yaml = new Yaml();
        return yaml.dump(data);
    }

    @SuppressWarnings("deprecation")
    public static <T> String generateJsonSchema(Class<T> clazz) throws MangleException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        com.fasterxml.jackson.databind.jsonschema.JsonSchema schema;
        try {
            schema = mapper.generateJsonSchema(clazz);
            return mapper.writeValueAsString(schema);
        } catch (IOException e) {
            throw new MangleException(String.format("Failed to generate Json schema : %s", clazz.getName()), e);
        }
    }

    public static <T> String generateExampleJsonData(Class<T> clazz) throws MangleException {
        return generateExampleJsonData(generateJsonSchema(clazz));
    }

    public static String generateExampleJsonData(String jsonSchema) throws MangleException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(jsonSchema);
            return "{ \n" + traverseJsonNode(jsonNode) + "}";
        } catch (IOException e) {
            throw new MangleException(String.format("Failed to generate sample JsonData from Schema : %s", jsonSchema),
                    e);
        }
    }

    private static String traverseJsonNode(JsonNode node) {
        StringBuilder sampleData = new StringBuilder();
        Iterator<Entry<String, JsonNode>> toplevelFields = node.fields();
        while (toplevelFields.hasNext()) {
            Iterator<Entry<String, JsonNode>> childFields = toplevelFields.next().getValue().fields();
            while (childFields.hasNext()) {
                Entry<String, JsonNode> l = childFields.next();
                JsonNode node1 = l.getValue();
                String typeString = node1.get("type").toString();
                String key = l.getKey();
                sampleData.append("  \"");
                if (typeString.contains("object")) {
                    sampleData.append(key).append("\": {\n").append(traverseJsonNode(node1)).append(" }");
                } else if (typeString.contains("array")) {
                    sampleData.append(key).append("\": [ ").append(extractItemsTypeFromArrayNode(node1)).append(" ]");
                } else if (node1.has("enum")) {
                    sampleData.append(key).append("\": ").append(extractEnumFromField(node1));
                } else if (typeString.contains("integer")) {
                    sampleData.append(key).append("\": ").append(0);
                } else if (typeString.contains("boolean")) {
                    sampleData.append(key).append("\": ").append("true");
                } else {
                    sampleData.append(key).append("\": ").append(typeString);
                }
                if (childFields.hasNext()) {
                    sampleData.append(",");
                }
                sampleData.append("\n");
            }
        }
        return sampleData.toString();
    }

    private static String extractItemsTypeFromArrayNode(JsonNode node) {
        return node.get("items").get("type").toString();
    }

    private static String extractEnumFromField(JsonNode node) {
        return node.get("enum").get(0).toString();
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

    public ResponseEntity<?> get(String urlSuffix, Class<?> clz) {
        log.info("");
        log.info("*****************API GET STARTS*************************************");
        setHttpRequestMethod(HttpMethod.GET);
        setRequestUrl(this.baseUrl + urlSuffix);
        return execute(clz);
    }

    public ResponseEntity<?> put(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.info("");
        log.info("*****************API PUT STARTS*************************************");
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity<?> putNoWait(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.info("");
        log.info("*****************API PUT STARTS*************************************");
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity<?> postNoWaitForSync(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);

    }

    public ResponseEntity<?> post(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity<?> post(String urlSuffix, String jsonRequest, Class<?> clz, MediaType mediaType) {
        log.info("Inside POST Method");
        this.headers.setContentType(mediaType);
        return post(urlSuffix, jsonRequest, clz);
    }

    public ResponseEntity<?> postFile(String urlSuffix, File file, Class<?> clz) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("refDBFile", resource);
        ResponseEntity<?> response = restTemplate.postForEntity(this.baseUrl + urlSuffix, form, String.class);
        checkStatus(response);
        return response;
    }

    public ResponseEntity<?> postForEntity(String urlSuffix, MultiValueMap<String, Object> formData, Class<?> clz) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        this.headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
        this.formData = formData;
        return execute(clz);
    }

    public ResponseEntity<?> postForEntity(String urlSuffix, MultiValueMap<String, Object> formData, Class<?> clz,
            MediaType mediaType) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        this.headers.setContentType(mediaType);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
        this.formData = formData;
        return execute(clz);
    }

    public ResponseEntity<?> putFile(String urlSuffix, String fileKey, File file, Class<?> clz) {
        log.info("");
        log.info("*****************API PUT FILE STARTS*************************************");
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        LinkedMultiValueMap<String, Object> multiPartMap = new LinkedMultiValueMap<>();
        multiPartMap.add(fileKey, file);
        this.entity = new HttpEntity<LinkedMultiValueMap<String, Object>>(multiPartMap, headers);
        return execute(clz);
    }

    public ResponseEntity<?> delete(String urlSuffix, String jsonRequest, Class<?> clz) {
        log.info("");
        log.info("*****************API DELETE STARTS*************************************");
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(clz);
    }

    public ResponseEntity<?> delete(String urlSuffix, Class<?> clz) {
        log.info("");
        log.info("*****************API DELETE STARTS*************************************");
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        return execute(clz);
    }

    public ResponseEntity<?> get(String urlSuffix, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API GET STARTS*************************************");
        setHttpRequestMethod(HttpMethod.GET);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = null;
        return execute(responseType);
    }

    public ResponseEntity<?> put(String urlSuffix, String jsonRequest, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API PUT STARTS*************************************");
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity<?> putNoWait(String urlSuffix, String jsonRequest,
            ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API PUT STARTS*************************************");
        setHttpRequestMethod(HttpMethod.PUT);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity<?> postNoWaitForSync(String urlSuffix, String jsonRequest,
            ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);

    }

    public ResponseEntity<?> post(String urlSuffix, String jsonRequest, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        setHttpRequestMethod(HttpMethod.POST);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity<?> postFile(String urlSuffix, File file, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API POST STARTS*************************************");
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("refDBFile", resource);
        ResponseEntity<?> response = restTemplate.postForEntity(this.baseUrl + urlSuffix, form, String.class);
        checkStatus(response);
        return response;
    }

    public ResponseEntity<?> delete(String urlSuffix, String jsonRequest, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API DELETE STARTS*************************************");
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        this.jsonRequest = jsonRequest;
        return execute(responseType);
    }

    public ResponseEntity<?> delete(String urlSuffix, ParameterizedTypeReference<?> responseType) {
        log.info("");
        log.info("*****************API DELETE STARTS*************************************");
        setHttpRequestMethod(HttpMethod.DELETE);
        setRequestUrl(this.baseUrl + urlSuffix);
        return execute(responseType);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<?> execute(Class<?> clz) {
        ResponseEntity<?> response = null;
        try {
            log.info("URL - " + getHttpRequestMethod().toString() + " - " + getRequestUrl());
            log.info(headers.getContentType().toString());
            String mediaType = headers.getContentType().toString();
            if (mediaType.contains(MediaType.APPLICATION_JSON_VALUE) && jsonRequest != "" && jsonRequest != null) {
                entity = new HttpEntity<String>(jsonRequest, headers);
            } else if (mediaType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)
                    || mediaType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
            } else {
                entity = new HttpEntity<String>(headers);
            }
            setAnnoationClass(clz);
            response = restTemplate.exchange(getRequestUrl(), getHttpRequestMethod(), entity, clz);
            if (null != response.getBody()) {
                log.info("Response body -" + objectToJson(response.getBody()));
            }
            return response;

        } catch (Exception e) {
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
                log.error(e);
                return response;
            } else {
                log.error(e);
            }
        } finally {
            log.info("*****************API ENDS     *************************************");
            log.info("");
        }
        return response;
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<?> execute(ParameterizedTypeReference<?> responseType) {
        ResponseEntity<?> response = null;
        try {
            log.info("URL - " + getHttpRequestMethod().toString() + " - " + getRequestUrl());
            log.info(headers.getContentType().toString());
            String mediaType = headers.getContentType().toString();
            if (mediaType.contains(MediaType.APPLICATION_JSON_VALUE) && jsonRequest != "" && jsonRequest != null) {
                entity = new HttpEntity<String>(jsonRequest, headers);
            } else if (mediaType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                entity = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
            } else {
                entity = new HttpEntity<String>(headers);
            }
            // setAnnoationClass(clz);
            response =
                    restTemplate.exchange(getRequestUrl(), getHttpRequestMethod(), entity, responseType, new Object[0]);
            log.info("Response body -" + objectToJson(response.getBody()));
            return response;

        } catch (Exception e) {
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
                log.error(e);
                return response;
            } else {
                log.error(e);
            }
        } finally {
            log.info("*****************API ENDS     *************************************");
            log.info("");
        }
        return response;
    }

    private void checkStatus(ResponseEntity<?> response) {

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("response status success");

        } else if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            log.info("response status success");
        } else {
            log.error("response status is unsuccessful");
        }

    }

    // /* (non-Javadoc)
    // * @see
    // com.vmware.itfm.cloud.interfaces.APIInterface#getHttpRequestMethod()
    // */
    public HttpMethod getHttpRequestMethod() {
        return this.method;
    }

    // /*
    // * (non-Javadoc)
    // * @see
    // * com.vmware.itfm.cloud.interfaces.APIInterface#setHttpRequestMethod(org
    // * .springframework.http.HttpMethod)
    // */
    public void setHttpRequestMethod(HttpMethod method) {
        this.method = method;

    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
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

    public void setJsonRequest(String jsonRequest) {
        this.jsonRequest = jsonRequest;
    }
}
