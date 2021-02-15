package com.vmware.mangle.resiliency.score.utils;

import com.vmware.mangle.metric.common.Metric;
import com.vmware.mangle.metrics.models.*;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author dbhat
 * 
 *         Utility class having methods to calculate resiliency score. Resiliency score calculating
 *         begins with retrieving all the fault injection events. Each of the fault injection events
 *         are parsed to retrieve time series data for each of the queries defined. The time series
 *         data corresponding to each of the queries will help in calculating the Resiliency score.
 */

@Log4j2
@Data
public class ResiliencyScoreUtils {
    private ResiliencyScoreProperties properties;
    private long startTime;
    private long endTime;
    private IMetricProviderHelper metricProvider;
    private ServiceFamily serviceFamily;
    private Double totalWeightOfQueries;
    private Services service;

    public ResiliencyScoreUtils(ResiliencyScoreProperties properties, ServiceFamily serviceFamily, Services service,
            long startTime, long endTime) {
        this.properties = properties;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceFamily = serviceFamily;
        this.service = service;
        initMetricProvider();
        this.totalWeightOfQueries = 0.0;
    }

    /**
     * Get Active metric provider defined from properties / Settings in Mangle.
     */
    private void initMetricProvider() {
        log.debug("Getting the Active Metric Provider");
        this.metricProvider =
                MetricProviderFactory.getActiveMetricProvider(properties.getMonitoringToolConnectionProperties());
    }

    public void calculateResiliencyScore() {
        if (null == this.metricProvider) {
            log.error("There are NO active metric provider found. Cannot proceed with calculating resiliency score");
            return;
        }
        log.info("Calculating the resiliency score for service: " + service.getName() + " part of service family: "
                + serviceFamily.getName());

        log.info(" Retrieving the events for the service: " + service.getName() + " in service family: "
                + serviceFamily.getName());
        List<WavefrontEvent> events = metricProvider.getEvents(properties.getTags(), serviceFamily.getName(),
                service.getName(), startTime, endTime);
        if (CollectionUtils.isEmpty(events)) {
            log.info(" No events found for the service: " + service.getName() + " under service Family: "
                    + serviceFamily.getName() + " between start time: " + startTime + " endTime : " + endTime);
            return;
        }
        log.debug(" Setting the queries to be executing while calculating the Resiliency score for the service: "
                + service.getName());
        service.setServiceSpecificQueries(getAllQueriesForService());
        log.debug(" Queries to be executed are: " + service.getServiceSpecificQueries().toString());
        List<Double> rScoresForQueries = processEventsToGetRScore(events);
        double rScoreForService = addWeightToRScoresCalculated(rScoresForQueries);
        log.debug(" Resiliency score for the service: " + service.getName() + " is: " + rScoreForService);
        sendMetric(rScoreForService);
    }

    /**
     * Sending the Resiliency score metric for the service. The Metric name is retrieved from the
     * Resiliency score properties file. The metric will have tags as : serviceFamily: service Family
     * Name, service: service Name. The source for metric is set to "mangle".
     * 
     * @param rScoreOfService
     *            : Resiliency score for the service.
     */
    private void sendMetric(double rScoreOfService) {

        Metric metric = new Metric(properties.getResiliencyScoreMetricConfig().getOutputMetricName(), rScoreOfService,
                getTags(), ResiliencyConstants.METRIC_SOURCE);
        metricProvider.sendMetric(metric);
    }

    /**
     * Constructs the tags to be associated while sending the resiliency score metric.
     * 
     * @return : Map having tags to be associated while sending the resiliency score metric.
     */
    private HashMap<String, String> getTags() {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(ResiliencyConstants.SERVICE_FAMILY, serviceFamily.getName());
        tags.put(ResiliencyConstants.SERVICE, service.getName());
        return tags;
    }

    /**
     * Each fault event will be processed. Based on the start and end time of fault injected,
     * PreInjection and Post Injection Resiliency score will be calculated. Pre Injection and Post
     * Injection Resiliency scores are then used to calculate the overall resiliency score for each of
     * the service.
     * 
     * @param events
     *            : Fault events for the specified service.
     * 
     * @return : Resiliency score for each of the queries specified for the service.
     */

    private List<Double> processEventsToGetRScore(List<WavefrontEvent> events) {
        log.debug(" Processing all the events for calculating the resiliency score");
        List<Double> rScoreForQueries = new ArrayList<>();
        for (WavefrontEvent event : events) {
            log.debug("Event start time: " + event.getStart() + " end time: " + event.getEnd());
            long preInjectionTime = getPreInjectionTimeWindow(event.getStart());
            long postInjectionTime = getPostInjectionTimeWindow(event.getEnd());
            log.debug("PreInjection time: " + preInjectionTime + " and Post Injection time: " + postInjectionTime);

            for (QueryParameters query : service.getServiceSpecificQueries()) {
                log.debug(" !!!!!!!!!! Query is: " + query.getQuery() + " !!!!!!!!!!! ");
                List<QueryResiliencyScore> preInjectionScoreOfQuery =
                        getRScoreForQuery(query.getQuery(), preInjectionTime, event.getStart());
                List<QueryResiliencyScore> postInjectionScoreOfQuery =
                        getRScoreForQuery(query.getQuery(), event.getEnd(), postInjectionTime);


                List<Double> rScoreOfQuery = getOverallRScoreForQuery(preInjectionScoreOfQuery,
                        postInjectionScoreOfQuery, query.getWeight());
                log.debug("Resiliency score of query: " + query.getQuery() + "is: " + rScoreOfQuery.toString());
                rScoreForQueries.addAll(rScoreOfQuery);
            }
        }
        return rScoreForQueries;
    }

    /**
     * Method to get final resiliency score for the mentioned service by applying the weight specified.
     * 
     * @param rScores
     *            : Resiliency score for each of the queries.
     * @return : Resiliency score for the service.
     */
    private double addWeightToRScoresCalculated(List<Double> rScores) {
        log.debug("Applying the weights to Resiliency scores");
        if (CollectionUtils.isEmpty(service.getServiceSpecificQueries())) {
            return 0.0;
        }
        double totalWeights = 0.0;
        double sumOfAllScores = 0.0;
        for (QueryParameters queryParameter : service.getServiceSpecificQueries()) {
            totalWeights = totalWeights + queryParameter.getWeight();
        }
        for (Double score : rScores) {
            sumOfAllScores = sumOfAllScores + score;
        }
        log.debug(
                "Sum of all the scores is : " + sumOfAllScores + " and Total weightages is : " + totalWeightOfQueries);
        log.debug("Formatted: " + roundOfTo2DecimalPlaces(sumOfAllScores) + " and "
                + roundOfTo2DecimalPlaces(totalWeightOfQueries));
        return sumOfAllScores / totalWeightOfQueries;

    }

    /**
     * Round off the specified number to 2 decimal places.
     * 
     * @param numberToRoundOff
     *            : number to be rounded off for 2 decimal places.
     * @return : Rounded off equivalent number.
     */
    private Double roundOfTo2DecimalPlaces(Double numberToRoundOff) {
        DecimalFormat dFormat = new DecimalFormat("##.00");
        String formatted = dFormat.format(numberToRoundOff);
        return Double.parseDouble(formatted);
    }

    /**
     * Get Resiliency score for the query / alert condition executed. Here, each time series in the Pre
     * Injection and Post injection data are compared and Resiliency score are calculated. Since, a
     * query / alert condition can return multiple time series data with different tags, Resiliency
     * score will be calculated for each of the time series in the query result.
     * 
     * @param preInjectionScore
     *            : Resiliency score for each of the time series data with tags - before fault
     *            injection.
     * @param postInjectionScore
     *            : Resiliency score for each of the time series data with tags - post fault execution
     * @return : Final resiliency score computed for the query.
     */
    private List<Double> getOverallRScoreForQuery(List<QueryResiliencyScore> preInjectionScore,
            List<QueryResiliencyScore> postInjectionScore, float queryWeightage) {
        log.debug("Getting the resiliency score for the fault duration");
        List<Double> rScores = new ArrayList<>();
        for (QueryResiliencyScore score : preInjectionScore) {
            for (QueryResiliencyScore pScore : postInjectionScore) {
                if (score.getTags().equals(pScore.getTags())) {
                    log.debug(" calculating the RScore for time series data ");
                    Double rScore = getPercentageOfRScore(score.getRScore(), pScore.getRScore());

                    if (rScore > 1) {
                        log.debug(
                                " Resiliency score is greater than 1 which is invalid score. Hence, rejecting the score");
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
     * Method to find the percentage - Resiliency Score for the pre injection and post injection scores
     * specified.
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
        return preInjectionScore / postInjectionScore;
    }

    /**
     * Method to find the Pre Injection time window for calculating the resiliency score. The time is
     * calculated as injection start time - duration specified in the rscore-properties.yaml file. By
     * default, it's 15 mins as configured in properties file.
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
     * Method to find the post injection time window. The time is calculated as : fault execution end
     * time + duration specified in the configuration / rscore-properties.yaml file. By default, it's 15
     * mins (as in properties file).
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
     * The method will combine all the common queries and service specific queries applicable for the
     * service specified.
     * 
     * @return : List of queries / alert conditions to be executed to calculate the Resiliency Score
     *         (RScore).
     */
    private List<QueryParameters> getAllQueriesForService() {
        List<QueryParameters> queries = new ArrayList<>();
        if (CollectionUtils.isEmpty(serviceFamily.getCommonQueries())) {
            if (CollectionUtils.isEmpty(service.getServiceSpecificQueries())) {
                return new ArrayList<>();
            }
            return service.getServiceSpecificQueries();
        } else if (CollectionUtils.isEmpty(service.getServiceSpecificQueries())) {
            return serviceFamily.getCommonQueries();
        }
        queries.addAll(serviceFamily.getCommonQueries());
        queries.addAll(service.getServiceSpecificQueries());
        return queries;
    }


    /**
     * Calculate Resiliency Score (RScore) for each of the time series in the query returned. The RScore
     * will be stored along with tags.
     * 
     * @param query
     *            : Query for which the time series data to be retrieved. The query must be an alert
     *            condition. The time series returned should be 1 or 0
     * @param startTimeInEpoch
     *            : Start time for the query to retrieve the time series data.
     * @param endTimeInEpoch
     *            : End time for the query to retrieve the time series data
     *
     * @return : List of Resiliency scores of each time series data for the query executed.
     */
    private List<QueryResiliencyScore> getRScoreForQuery(String query, long startTimeInEpoch, long endTimeInEpoch) {
        List<TimeseriesData> timeSeriesData = metricProvider.getTimeSeriesData(query, startTimeInEpoch, endTimeInEpoch,
                properties.getResiliencyScoreMetricConfig().getMetricQueryGranularity());
        log.debug("Time series retrieved for the query: " + query + "is " + timeSeriesData.toString());
        List<QueryResiliencyScore> rScores = new ArrayList<>();
        for (TimeseriesData eachTimeSeries : timeSeriesData) {
            log.debug("Calculating the Resiliency score for each of the time series data in query : " + query);

            QueryResiliencyScore score = new QueryResiliencyScore();
            if (CollectionUtils.isEmpty(eachTimeSeries.getTags())) {
                score.setTags(new HashMap<>());
            } else {
                score.setTags(eachTimeSeries.getTags());
            }
            if (eachTimeSeries.getData().length > 0) {
                score.setRScore(getRScoreForTimeSeries(eachTimeSeries.getData()));
            } else {
                log.debug(" Time Series data is empty. Hence, RScore is not applicable ");
                continue;
            }
            rScores.add(score);
        }
        return rScores;
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
        double countOf1s = 0.0;
        for (int row = 0; row < timeSeriesData.length; row++) {
            contents.append(timeSeriesData[row][1]);
            contents.append(", ");
            if (timeSeriesData[row][1] == 1) {
                countOf1s++;
            }
        }
        double rScore = (countOf1s / timeSeriesData.length);
        if (rScore == 0) {
            rScore = 1.0;
        }
        log.debug("Time series : " + contents.toString());
        log.debug("Number of 1s is : " + countOf1s + " and all the data points is: " + timeSeriesData.length);
        log.debug(" RScore for the time series is : " + rScore);
        return rScore;
    }


}
