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

package com.vmware.mangle.resiliency.score.email;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.exception.MangleErrorCodes;
import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.metrics.models.QueryResult;
import com.vmware.mangle.metrics.models.Timeseries;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import com.vmware.mangle.resiliency.score.utils.MailUtils;
import com.vmware.mangle.resiliency.score.utils.ReadProperty;

import lombok.extern.log4j.Log4j2;

/**
 * @author ranjans
 *
 */
@Log4j2
public class ResiliencyScoreEmail {

    private static final String EMAIL_SUBJECT = "Do you know the resiliency score for your services this week?";
    private static final String EMAIL_HEADER =
            "<table align=\"center\" width=\"69%\"><tr><th colspan=\"3\" bgcolor=\"black\" align=\"center\"><p><font size=\"6\" color=\"white\">SYMPHONY SERVICES RESILIENCY SCORE THIS WEEK</font></p></th></tr>";
    private static final String EMAIL_BODY_SEPARATOR = "<tr><td colspan=\"3\"><hr/></td></tr>";
    private String emailFooter =
            "<tr><td colspan=\"3\" bgcolor=\"black\" align=\"center\"><p><font size=\"2\" color=\"white\"><a href=\"WAVEFRONT_URL\" style=\"color:#87CEEB\">Click Here</a> for details access on the Wavefront dashboard<br/>Send Feedback <a href =\"https://vmware.slack.com/messages/C58FLC5D0\" style=\"color:#87CEEB\">#mangle channel</a> / <a href =\"mailto: es-blr-faultinjection@vmware.com\" style=\"color:#87CEEB\">email</a></font></p></td></tr></table>";
    private static final String DASH_AND_SPACE = "&emsp;&emsp;-&nbsp;&nbsp;";
    private static final String BR_DASH_AND_SPACE = "<br/>&emsp;&emsp;-&nbsp;&nbsp;";
    private static final String SERVICE_FAMILY_NAME = "serviceFamilyName";
    private static final String RESILIENCY_SCORE = "resiliencyScore";
    private static final String FAULT_INJECTION_COUNT = "faultInjectionCount";
    private static final String FAULT_SURVIVED_COUNT = "faultSurvivedCount";
    private static final String LETHAL_SERVICES = "lethalServices";
    private static final Properties SERVICE_MAPPING = ReadProperty.readProperty("service_mapping.properties");
    private WavefrontMetricProviderHelper waveFrontServerClient;
    private String wavefrontUrl;
    private long startTimeMillis;
    private String serviceEmailTemplate;
    private String resiliencyScoreImages;
    private String resiliencyScoreMetrics;
    private String berserkerApiMetrics;

    public ResiliencyScoreEmail(WavefrontMetricProviderHelper waveFrontServerClient, int emailDays,
            String serviceEmailTemplate, String resiliencyScoreImages, String resiliencyScoreMetrics,
            String berserkerApiMetrics) {
        this.waveFrontServerClient = waveFrontServerClient;
        this.wavefrontUrl = this.waveFrontServerClient.getBaseUrl();
        this.startTimeMillis = new DateTime().minusDays(emailDays).getMillis() / 1000;
        this.serviceEmailTemplate = serviceEmailTemplate;
        this.resiliencyScoreImages = resiliencyScoreImages;
        this.resiliencyScoreMetrics = resiliencyScoreMetrics;
        this.berserkerApiMetrics = berserkerApiMetrics;
    }

    public void sendResiliencyScoreEmail(String[] serviceFamilies, String emailId) throws MangleException {
        log.info("Mail send start...");
        HashMap<String, String> emailBodyImageMap = getResiliencyScoreEmailBodyImageMap(serviceFamilies);
        String emailBody = getResiliencyScoreEmailBody(serviceFamilies);
        log.info(emailBody);
        boolean mailStatus =
                MailUtils.mail(new String[] { emailId }, emailBody, EMAIL_SUBJECT, new String[0], emailBodyImageMap);
        if (mailStatus) {
            log.info("Resiliency score email successfully sent to: " + emailId);
        } else {
            throw new MangleException(MangleErrorCodes.SCORE_EMAIL_SENT_FAILED + emailId);
        }
    }

    private HashMap<String, String> getResiliencyScoreEmailBodyImageMap(String[] serviceFamilies) {
        HashMap<String, String> imageLocationAndCid = new HashMap<>();
        File folder = new File(this.resiliencyScoreImages);
        for (String service : serviceFamilies) {
            imageLocationAndCid.put(folder + "/" + service + ".jpg", service);
        }
        imageLocationAndCid.put(folder + "/Injection.jpg", "Injection");
        imageLocationAndCid.put(folder + "/Score.jpg", "Score");
        imageLocationAndCid.put(folder + "/Skull.jpg", "Skull");
        imageLocationAndCid.put(folder + "/Star.jpg", "Star");
        return imageLocationAndCid;
    }

    private String getResiliencyScoreEmailBody(String[] serviceFamilies) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append(EMAIL_HEADER);
        String serviceTemplateContent = readFileAsString(this.serviceEmailTemplate);
        for (int i = 0; i < serviceFamilies.length; i++) {
            HashMap<String, String> replaceableKeyValue = new HashMap<>();
            replaceableKeyValue.put(SERVICE_FAMILY_NAME, serviceFamilies[i]);
            String[] services = SERVICE_MAPPING.getProperty(serviceFamilies[i]).trim().split(",");
            replaceableKeyValue.put(RESILIENCY_SCORE, getServiceFamilyResiliencyScore(serviceFamilies[i]) + "%");
            replaceableKeyValue.put(FAULT_INJECTION_COUNT, getTotalFaultInjectionEvents(serviceFamilies[i]));
            replaceableKeyValue.put(FAULT_SURVIVED_COUNT, getBerserkerAPIResults(services));
            replaceableKeyValue.put(LETHAL_SERVICES, getLethalServices(services));
            if (i > 0 && i <= serviceFamilies.length) {
                emailBody.append(EMAIL_BODY_SEPARATOR);
            }
            emailBody.append(getReplacedContent(serviceTemplateContent, replaceableKeyValue));
        }
        emailFooter = emailFooter.replace("WAVEFRONT_URL", wavefrontUrl + "/dashboard/ResiliencyScore");
        emailBody.append(emailFooter);
        return emailBody.toString();
    }

    private String getServiceFamilyResiliencyScore(String serviceFamily) {
        log.info("Getting service family resiliency score...");
        QueryResult queryResult = getQueryResult(String.format(ResiliencyConstants.SERVICE_FAMILY_RELISIENCY_SCORE,
                this.startTimeMillis, this.resiliencyScoreMetrics, serviceFamily));
        double resiliencyScore = getTimeseriesDataAvg(queryResult.getTimeseries());
        return String.valueOf(round3Decimal(resiliencyScore * 100));
    }

    private String getTotalFaultInjectionEvents(String serviceFamily) {
        log.info("Getting total fault injection events...");
        QueryResult queryResult = getQueryResult(
                String.format(ResiliencyConstants.SERVICE_FAMILY_INJECTION_EVENT, this.startTimeMillis, serviceFamily));
        return String.valueOf(queryResult.getEvents().length);
    }

    private String getBerserkerAPIResults(String[] services) {
        log.info("Getting berserker API results...");
        long totalApiExecuted = 0;
        for (int i = 0; i < services.length; i++) {
            totalApiExecuted = totalApiExecuted + getAPICount(services[i]);
        }
        long passApi = 0;
        for (int i = 0; i < services.length; i++) {
            passApi = passApi + getPassAPICount(services[i]);
        }
        double passPercent = 0;
        if (totalApiExecuted > 0) {
            passPercent = (passApi * 100.0) / totalApiExecuted;
        }
        return DASH_AND_SPACE + "Total API executed: " + totalApiExecuted + BR_DASH_AND_SPACE + "Pass API: "
                + round3Decimal(passPercent) + " %";
    }

    private long getAPICount(String service) {
        QueryResult queryResult = getQueryResult(String.format(ResiliencyConstants.SERVICE_TOTAL_API_COUNT,
                this.startTimeMillis, this.berserkerApiMetrics, service));
        if (queryResult.getTimeseries() != null) {
            return queryResult.getTimeseries().length;
        } else {
            return 0;
        }
    }

    private long getPassAPICount(String service) {
        QueryResult queryResult = getQueryResult(String.format(ResiliencyConstants.SERVICE_PASS_API_COUNT,
                this.startTimeMillis, this.berserkerApiMetrics, service));
        if (queryResult.getTimeseries() != null) {
            return queryResult.getTimeseries().length;
        } else {
            return 0;
        }
    }

    private String getLethalServices(String[] services) {
        log.info("Getting lethal services...");
        Map<String, Double> scoreMap = new HashMap<>();
        for (String service : services) {
            Double resiliencyScore = getResiliencyScore(service);
            if (resiliencyScore != null) {
                scoreMap.put(service, resiliencyScore);
            }
        }
        Map<String, Double> sortedMap = scoreMap.entrySet().stream().sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        String lethalServices = DASH_AND_SPACE;
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
            if (count == 0) {
                lethalServices = lethalServices + entry.getKey();
            }
            if (count == 1) {
                lethalServices = lethalServices + BR_DASH_AND_SPACE + entry.getKey();
            }
            if (count >= 2) {
                break;
            }
            count++;
        }
        return lethalServices;
    }

    private Double getResiliencyScore(String service) {
        QueryResult queryResult = getQueryResult(String.format(ResiliencyConstants.SERVICE_RELISIENCY_SCORE,
                this.startTimeMillis, this.resiliencyScoreMetrics, service));
        if (queryResult.getTimeseries() != null) {
            return getTimeseriesDataAvg(queryResult.getTimeseries());
        } else {
            return null;
        }
    }

    private String readFileAsString(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (IOException e) {
            try {
                throw new MangleException(MangleErrorCodes.READ_FILE_AS_STRING_FAILED);
            } catch (MangleException mangleException) {
                log.error("Mangle error code: " + mangleException.getErrorCode());
            }
        }
        return stringBuilder.toString();
    }

    private String getReplacedContent(String sourceContent, HashMap<String, String> keyValue) {
        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey() != null) {
                sourceContent = sourceContent.replace(entry.getKey(), entry.getValue());
            }
        }
        return sourceContent;
    }

    private QueryResult getQueryResult(String queryApiUrl) {
        log.info(queryApiUrl);
        return (QueryResult) this.waveFrontServerClient.get(queryApiUrl, QueryResult.class).getBody();
    }

    private double getTimeseriesDataAvg(Timeseries[] timeseriesArray) {
        double dataValTotal = 0;
        int count = 0;
        if (null != timeseriesArray && timeseriesArray.length != 0) {
            for (Timeseries timeseries : timeseriesArray) {
                Double[][] dataArray = timeseries.getData();
                for (int row = 0; row < dataArray.length; row++) {
                    dataValTotal = dataValTotal + dataArray[row][1];
                    count++;
                }
            }
        }
        return count != 0 ? dataValTotal / count : 0;
    }

    private double round3Decimal(double input) {
        return Math.round(input * 1000.0) / 1000.0;
    }


}
