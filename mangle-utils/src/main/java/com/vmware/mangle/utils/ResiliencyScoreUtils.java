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

package com.vmware.mangle.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.resiliencyscore.FaultEventResiliencyScore;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryResiliencyScore;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.resiliencyscore.ServiceResiliencyScore;
import com.vmware.mangle.model.ResiliencyScoreVO;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.model.resiliencyscore.TimeSeriesResiliencyScore;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.ResiliencyConstants;
import com.vmware.mangle.utils.helpers.metricprovider.IMetricProviderHelper;
import com.vmware.mangle.utils.helpers.metricprovider.MetricProviderFactory;



/**
 * @author dbhat
 *         <p>
 *         Utility class having methods to calculate resiliency score. Resiliency score calculating
 *         begins with retrieving all the fault injection events. Each of the fault injection events
 *         are parsed to retrieve time series data for each of the queries defined. The time series
 *         data corresponding to each of the queries will help in calculating the Resiliency score.
 */

@Log4j2
public class ResiliencyScoreUtils {
    private ResiliencyScoreProperties properties;
    private long startTime;
    private long endTime;
    @Setter
    private IMetricProviderHelper metricProvider;
    private Double totalWeightOfQueries;
    private Service service;
    private ServiceResiliencyScore serviceResiliencyScore;
    private List<QueryDto> allQueries;
    private String statusMessage;

    public ResiliencyScoreUtils(ResiliencyScoreProperties properties, Service service, List<QueryDto> allQueries,
            long startTime, long endTime) {
        this.properties = properties;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
        this.allQueries = allQueries;
        initMetricProvider();
        this.totalWeightOfQueries = 0.0;
    }

    /**
     * Get Active metric provider defined from properties / Settings in Mangle.
     */
    private void initMetricProvider() {
        log.debug("Getting the Active Metric Provider");
        this.metricProvider = MetricProviderFactory.getActiveMetricProvider(properties.getMetricProviderSpec());
    }

    public ResiliencyScoreVO calculateResiliencyScore() {
        if (null == this.metricProvider) {
            log.error(ErrorConstants.NO_ACTIVE_METRIC_PROVIDER + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE);
            return setResiliencyScore(ResiliencyConstants.INVALID_SCORE,
                    ErrorConstants.NO_ACTIVE_METRIC_PROVIDER + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE, null);
        }
        log.info("Calculating the resiliency score for service: " + service.getName());

        log.info(" Retrieving the events for the service: " + service.getName());
        List<WavefrontEvent> events = metricProvider.getEvents(getAllTags(properties.getTags(), service.getTags()),
                null, null, startTime, endTime);
        if (CollectionUtils.isEmpty(events)) {
            String message = ErrorConstants.NO_FAULT_EVENTS_FOUND
                    + String.format(ErrorConstants.TIME_DURATION, startTime, endTime)
                    + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE;
            log.error(message);
            return setResiliencyScore(ResiliencyConstants.INVALID_SCORE, message, null);
        }
        log.debug(" Setting the queries to be executing while calculating the Resiliency score for the service: "
                + service.getName());
        serviceResiliencyScore = new ServiceResiliencyScore();
        serviceResiliencyScore.setServiceName(service.getName());
        log.debug(" Queries to be executed are: " + allQueries.toString());
        List<Double> rScoresForQueries = processEventsToGetRScore(events);
        double rScoreForService = addWeightToRScoresCalculated(rScoresForQueries);
        log.info(" Resiliency score for the service: " + service.getName() + " is: " + rScoreForService);
        log.debug(serviceResiliencyScore.toString());
        return setResiliencyScore(rScoreForService, this.statusMessage, serviceResiliencyScore);
    }

    private Map<String, String> getAllTags(Map<String, String> propertyTags, Map<String, String> serviceTags) {
        Map<String, String> allTags = new HashMap<>();
        if (CollectionUtils.isEmpty(propertyTags) && CollectionUtils.isEmpty(serviceTags)) {
            log.debug("Property tags and service tags are empty.");
            return allTags;
        }
        if (CollectionUtils.isEmpty(serviceTags)) {
            log.debug("Service tags are empty. Hence returning only the property tags");
            return propertyTags;
        }
        if (CollectionUtils.isEmpty(propertyTags)) {
            log.debug("Property tags are empty. Returning service tags");
            return serviceTags;
        }
        allTags.putAll(serviceTags);
        allTags.putAll(propertyTags);
        log.debug("All the tags to be associated: " + allTags);
        return allTags;
    }

    private ResiliencyScoreVO setResiliencyScore(double resiliencyScore, String message,
            ServiceResiliencyScore serviceResiliencyScore) {
        ResiliencyScoreVO resiliencyScoreVO = new ResiliencyScoreVO();
        resiliencyScoreVO.setResiliencyScore(resiliencyScore);
        resiliencyScoreVO.setMessage(message);
        if (null == serviceResiliencyScore) {
            resiliencyScoreVO.setServiceResiliencyScore(new ServiceResiliencyScore());
        } else {
            resiliencyScoreVO.setServiceResiliencyScore(serviceResiliencyScore);
        }
        return resiliencyScoreVO;
    }

    /**
     * Each fault event will be processed. Based on the start and end time of fault injected,
     * PreInjection and Post Injection Resiliency score will be calculated. Pre Injection and Post
     * Injection Resiliency scores are then used to calculate the overall resiliency score for each
     * of the service.
     *
     * @param events
     *            : Fault events for the specified service.
     * @return : Resiliency score for each of the queries specified for the service.
     */

    private List<Double> processEventsToGetRScore(List<WavefrontEvent> events) {
        log.debug(" Processing all the events for calculating the resiliency score");

        List<Double> rScoreForQueries = new ArrayList<>();
        List<FaultEventResiliencyScore> faultEventResiliencyScores = new ArrayList<>();
        // Iterate through each of the fault injection events to calculate the pre-injection and post injection resiliency scores.
        for (WavefrontEvent event : events) {
            log.info("Processing the fault injection event: " + event.getName() + " with event start time: "
                    + event.getStart() + " end time: " + event.getEnd());
            long preInjectionTime = getPreInjectionTimeWindow(event.getStart());
            long postInjectionTime = getPostInjectionTimeWindow(event.getEnd());
            log.debug("PreInjection time: " + preInjectionTime + " and Post Injection time: " + postInjectionTime);
            FaultEventResiliencyScore faultEventResiliencyScore = new FaultEventResiliencyScore();
            faultEventResiliencyScore.setFaultInjectionEventName(event.getName());
            List<QueryResiliencyScore> queryResiliencyScoreList = new ArrayList<>();
            for (QueryDto query : allQueries) {
                QueryResiliencyScore queryResiliencyScore = new QueryResiliencyScore();
                queryResiliencyScore.setQueryName(query.getQueryCondition());
                log.info(" Processing Resiliency score for Query: " + query.getQueryCondition());
                log.info("Processing Pre-Injection score");
                List<TimeSeriesResiliencyScore> preInjectionScoreOfQuery =
                        retrieveResiliencyScoreForQuery(query.getQueryCondition(), preInjectionTime, event.getStart());

                log.info("Processing Post-Injection score");
                List<TimeSeriesResiliencyScore> postInjectionScoreOfQuery =
                        retrieveResiliencyScoreForQuery(query.getQueryCondition(), event.getEnd(), postInjectionTime);

                List<Double> rScoreOfQueryList = getOverallRScoreForQuery(preInjectionScoreOfQuery,
                        postInjectionScoreOfQuery, query.getWeight());
                queryResiliencyScore.setResiliencyScore(rScoreOfQueryList);
                queryResiliencyScoreList.add(queryResiliencyScore);
                log.info("Resiliency score of query: " + query.getQueryCondition() + "is: "
                        + rScoreOfQueryList.toString());
                rScoreForQueries.addAll(rScoreOfQueryList);
            }
            faultEventResiliencyScore.setQueryResiliencyScore(queryResiliencyScoreList);
            faultEventResiliencyScores.add(faultEventResiliencyScore);
        }
        serviceResiliencyScore.setFaultInjectionEventResiliencyScore(faultEventResiliencyScores);
        return rScoreForQueries;
    }

    /**
     * Method to retrieve the time series data for the query condition specified for the specified
     * duration of time. If the time series data retrieved from the monitoring tools is empty or no
     * data, the method will do retry attempts. If the time series data is empty even after
     * configured retry attempts, the resiliency score for the query is marked as invalid.
     *
     * @param query
     *            : Query to be run in the configured monitoring tool.
     * @param startTimeInEpoch
     *            : Start time for retrieving the time series data.
     * @param endTimeInEpoch
     *            : End time for retrieving the time series data.
     * @return : Time series resiliency score.
     */

    private List<TimeSeriesResiliencyScore> retrieveResiliencyScoreForQuery(String query, long startTimeInEpoch,
            long endTimeInEpoch) {
        List<TimeSeriesResiliencyScore> resiliencyScoreOfQuery =
                getResiliencyScoresListForQuery(query, startTimeInEpoch, endTimeInEpoch);
        if (CollectionUtils.isEmpty(resiliencyScoreOfQuery)) {
            int retryCount = 0;
            while (retryCount < ResiliencyConstants.QUERY_RETRY_COUNT) {
                log.error(ErrorConstants.INVALID_TIME_SERIES_DATA + ErrorConstants.RETRYING);
                resiliencyScoreOfQuery = getResiliencyScoresListForQuery(query, startTimeInEpoch, endTimeInEpoch);
                if (!CollectionUtils.isEmpty(resiliencyScoreOfQuery)) {
                    return resiliencyScoreOfQuery;
                }
                retryCount++;
            }
            log.error(ErrorConstants.INVALID_TIME_SERIES_DATA + ErrorConstants.RETRYING_FAILED);
        }
        return resiliencyScoreOfQuery;
    }


    /**
     * Method to get final resiliency score for the mentioned service by applying the weight
     * specified.
     *
     * @param rScores
     *            : Resiliency score for each of the queries.
     * @return : Resiliency score for the service.
     */
    private double addWeightToRScoresCalculated(List<Double> rScores) {
        log.debug("Applying the weights to Resiliency scores");
        if (CollectionUtils.isEmpty(allQueries)) {
            return 0.0;
        }
        double totalWeights = 0.0;
        double sumOfAllScores = 0.0;
        for (QueryDto queryParameter : allQueries) {
            totalWeights = totalWeights + queryParameter.getWeight();
        }
        for (Double score : rScores) {
            sumOfAllScores = sumOfAllScores + score;
        }
        log.debug(
                "Sum of all the scores is : " + sumOfAllScores + " and Total weightages is : " + totalWeightOfQueries);
        log.debug("Formatted: " + roundOfToTwoDecimalPlaces(sumOfAllScores) + " and "
                + roundOfToTwoDecimalPlaces(totalWeightOfQueries));
        if (totalWeightOfQueries <= 0) {
            log.error(ErrorConstants.INVALID_WEIGHTS_FOUND);
            this.statusMessage = ErrorConstants.INVALID_TIME_SERIES_DATA + ErrorConstants.REFER_LOG_FOR_MORE_DETAILS;
            log.error(this.statusMessage);
            return ResiliencyConstants.INVALID_SCORE;
        }
        this.statusMessage = ResiliencyConstants.RESILIENCY_SCORE_SUCCESS_MESSAGE;
        return sumOfAllScores / totalWeightOfQueries;

    }

    /**
     * Round off the specified number to 2 decimal places.
     *
     * @param numberToRoundOff
     *            : number to be rounded off for 2 decimal places.
     * @return : Rounded off equivalent number.
     */
    private Double roundOfToTwoDecimalPlaces(Double numberToRoundOff) {
        DecimalFormat dFormat = new DecimalFormat("##.00");
        String formatted = dFormat.format(numberToRoundOff);
        return Double.parseDouble(formatted);
    }

    /**
     * Get Resiliency score for the query / alert condition executed. Here, each time series in the
     * Pre Injection and Post injection data are compared and Resiliency score are calculated.
     * Since, a query / alert condition can return multiple time series data with different tags,
     * Resiliency score will be calculated for each of the time series in the query result.
     *
     * @param preInjectionScoreList
     *            : Resiliency score for each of the time series data with tags - before fault
     *            injection.
     * @param postInjectionScoreList
     *            : Resiliency score for each of the time series data with tags - post fault
     *            execution
     * @return : Final resiliency score computed for the query.
     */
    private List<Double> getOverallRScoreForQuery(List<TimeSeriesResiliencyScore> preInjectionScoreList,
            List<TimeSeriesResiliencyScore> postInjectionScoreList, float queryWeightage) {
        log.debug("Getting the resiliency score for each of the timeSeries in the query");
        List<Double> rScores = new ArrayList<>();
        if (CollectionUtils.isEmpty(preInjectionScoreList) || CollectionUtils.isEmpty(postInjectionScoreList)) {
            log.error(ErrorConstants.INVALID_TIME_SERIES_DATA);
            log.debug("Hence, Resiliency score will not be calculated for the query");
            return rScores;
        }
        for (TimeSeriesResiliencyScore preInjectionScore : preInjectionScoreList) {
            for (TimeSeriesResiliencyScore postInjectionScore : postInjectionScoreList) {
                if (preInjectionScore.getTags().equals(postInjectionScore.getTags())) {
                    log.debug(" calculating the RScore for time series data ");
                    log.debug("Pre-Injection Score for : " + preInjectionScore.getTags() + ResiliencyConstants.IS
                            + preInjectionScore.getRScore());
                    log.debug("Post-Injection Score: " + postInjectionScore.getTags() + ResiliencyConstants.IS
                            + postInjectionScore.getRScore());
                    Double rScore =
                            getPercentageOfRScore(preInjectionScore.getRScore(), postInjectionScore.getRScore());

                    if (rScore > 1 || rScore < 0) {
                        log.debug(
                                " Resiliency score is greater than 1 or less than 1 indicates invalid score. Hence, rejecting the score");
                    } else {
                        rScores.add(rScore * queryWeightage);
                        this.totalWeightOfQueries = totalWeightOfQueries + queryWeightage;
                    }
                }
            }
        }
        return rScores;
    }

    /**
     * Method to find the percentage - Resiliency Score for the pre injection and post injection
     * scores specified.
     *
     * @param preInjectionScore
     *            : Resiliency score before injecting the fault.
     * @param postInjectionScore
     *            : Resiliency score post completion of fault execution.
     * @return : Resiliency score.
     */
    private Double getPercentageOfRScore(Double preInjectionScore, Double postInjectionScore) {
        log.debug("Getting the RScore for the preInjection score : " + preInjectionScore + " and Post Injection score: "
                + postInjectionScore);
        if (postInjectionScore == 0.0 || preInjectionScore == 0.0) {
            return 0.0;
        }
        if (preInjectionScore >= postInjectionScore) {
            return postInjectionScore / preInjectionScore;
        }
        log.debug(
                "Preinjection score is less than Post injection score. The fault injection impact cannot be determined with this data. The data need to be ignored.");
        return ResiliencyConstants.INVALID_SCORE;
    }

    /**
     * Method to find the Pre Injection time window for calculating the resiliency score. The time
     * is calculated as injection start time - duration specified in the rscore-properties.yaml
     * file. By default, it's 15 mins as configured in properties file.
     *
     * @param injectionStartTime
     *            : Time at which the fault injection has started. The data can be found in fault
     *            injection event.
     * @return : Pre injection time for calculating the Resiliency score.
     */
    private long getPreInjectionTimeWindow(long injectionStartTime) {
        long timeWindow =
                injectionStartTime - (properties.getResiliencyScoreMetricConfig().getTestReferenceWindow() * 60 * 1000);
        log.debug(" Pre Injection time window is: " + timeWindow);
        return timeWindow;
    }

    /**
     * Method to find the post injection time window. The time is calculated as : fault execution
     * end time + duration specified in the configuration / rscore-properties.yaml file. By default,
     * it's 15 mins (as in properties file).
     *
     * @param injectionEndTime
     *            : Fault injection end time. The data is available in the fault injection event
     *            retrieved.
     * @return : The resiliency score duration of time (post fault injection)
     */
    private long getPostInjectionTimeWindow(long injectionEndTime) {
        long timeWindow =
                injectionEndTime + (properties.getResiliencyScoreMetricConfig().getTestReferenceWindow() * 60 * 1000);
        log.debug(" Post Injection time window is: " + timeWindow);
        return timeWindow;
    }


    /**
     * Calculate Resiliency Score (RScore) for each of the time series in the query returned. The
     * RScore will be stored along with tags. Each of the alert / query condition api call to Metric
     * provider, return a series of time series data. The time series data returned might have sub
     * tags, hostname and associated time series values. We are considering all the time series data
     * here.
     *
     * @param query
     *            : QueryDto for which the time series data to be retrieved. The query must be an
     *            alert condition. The time series returned should be 1 or 0. The time series value
     *            1 represents alert condition is met and 0 otherwise.
     * @param startTimeInEpoch
     *            : Start time for the query to retrieve the time series data.
     * @param endTimeInEpoch
     *            : End time for the query to retrieve the time series data
     * @return : List of Resiliency scores of each time series data for the query executed.
     */
    private List<TimeSeriesResiliencyScore> getResiliencyScoresListForQuery(String query, long startTimeInEpoch,
            long endTimeInEpoch) {
        List<TimeseriesData> timeSeriesData = metricProvider.getTimeSeriesData(query, startTimeInEpoch, endTimeInEpoch,
                properties.getResiliencyScoreMetricConfig().getMetricQueryGranularity());
        log.debug("Time series retrieved for the query: " + query + "is " + timeSeriesData.toString());
        List<TimeSeriesResiliencyScore> resiliencyScoresListForQuery = new ArrayList<>();
        for (TimeseriesData eachTimeSeries : timeSeriesData) {
            log.debug("Calculating the Resiliency score for each of the time series data in query : " + query);

            TimeSeriesResiliencyScore score = new TimeSeriesResiliencyScore();
            if (CollectionUtils.isEmpty(eachTimeSeries.getTags())) {
                score.setTags(new HashMap<>());
            } else {
                score.setTags(eachTimeSeries.getTags());
            }
            if (eachTimeSeries.getData().length > 0) {
                score.setRScore(getRScoreForTimeSeries(eachTimeSeries.getData()));
            } else {
                log.trace(" Time Series data is empty. Hence, RScore is not applicable ");
                continue;
            }
            log.debug("Resiliency Score for the Timeseries with tags: " + score.getTags() + ResiliencyConstants.IS
                    + score.getRScore());
            resiliencyScoresListForQuery.add(score);
        }
        return resiliencyScoresListForQuery;
    }

    /**
     * Method to get resiliency score for the specified time series data. The logic calculates the
     * number of 1's (time series value) against the number of time series data returned. As a
     * convention, 1's indicate the alert triggered in the monitoring tool system.
     *
     * @param timeSeriesData
     *            : Time series data for which the resiliency score to be calculated.
     * @return : Resiliency score (number of 1's / total number of points in the time series data).
     */
    private Double getRScoreForTimeSeries(Double[][] timeSeriesData) {
        log.debug("Calculating the Resiliency score of the specified time series");
        StringBuilder contents = new StringBuilder("TimeSeries Data: ");
        double alertTriggeredCount = 0.0;
        for (int row = 0; row < timeSeriesData.length; row++) {
            contents.append(timeSeriesData[row][1]);
            alertTriggeredCount = alertTriggeredCount + timeSeriesData[row][1];
            contents.append(", ");
        }
        double rScore = ((timeSeriesData.length - alertTriggeredCount) / timeSeriesData.length);
        if (rScore == 0) {
            rScore = 1.0;
        }
        log.debug("Time series : " + contents.toString());
        log.debug("Number of times alerts triggered (1s) is : " + alertTriggeredCount + " and all the data points is: "
                + timeSeriesData.length);
        log.debug(" RScore for the time series is : " + rScore);
        return rScore;
    }

}
