package com.vmware.mangle.resiliency.services;


import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.metrics.models.ResiliencyScoreProperties;
import com.vmware.mangle.metrics.models.ServiceFamily;
import com.vmware.mangle.metrics.models.Services;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Log4j2
public class RScoreHelper {
    private ResiliencyScoreProperties properties;
    private long startTime;
    private long endTime;

    public RScoreHelper() throws MangleException {
        readResiliencyScoreConfiguration();
        initTimeWindow();
    }

    /**
     * Entry point for the Resiliency score calculator. Pool of threads are defined and each thread will
     * take care of calculating resiliency score for 1 service.
     */
    public void calculateRScore() {
        ExecutorService executorService = Executors.newFixedThreadPool(ResiliencyConstants.THREAD_POOL_SIZE);
        for (ServiceFamily serviceFamily : properties.getServiceFamily()) {
            for (Services service : serviceFamily.getServices()) {
                ResiliencyCalculatorBasedOnQueries rScoreCalculator =
                        new ResiliencyCalculatorBasedOnQueries(properties, serviceFamily, service, startTime, endTime);
                executorService.submit(rScoreCalculator);
            }
        }
        executorService.shutdown();
    }

    /**
     * Initialises the start time and end time for calculating the resiliency score. End time will be
     * initialised to current time and start time will be initialised to 1 hour (as defined in
     * properties file) earlier.
     */
    private void initTimeWindow() {
        log.debug("Finding the calender start time and endtime to retrieve the events from Metric provider.");
        Calendar calendar = Calendar.getInstance();
        this.endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -properties.getResiliencyScoreMetricConfig().getResiliencyCalculationWindow());
        this.startTime = calendar.getTimeInMillis();
        log.debug("Time window details - Start time: " + startTime + "  End Time: " + endTime);
    }

    /**
     * Reads the resiliency configurations from rscore-properties.yaml file (if not passed through
     * command line argument). The configurations / properties will be used for calculating the
     * resiliency score.
     *
     * @throws MangleException
     *             : MangleException will be thrown in case of failures.
     */
    private void readResiliencyScoreConfiguration() throws MangleException {
        log.info("Reading the configuration for resiliency calculator");
        String propertyFile = System.getProperty(ResiliencyConstants.PROPERTY_FILE);
        if (!StringUtils.hasText(propertyFile)) {
            propertyFile = ResiliencyConstants.RSCORE_PROPERTIES_FILE;
        }
        Yaml yaml = new Yaml(new Constructor(ResiliencyScoreProperties.class));
        try {
            FileInputStream fis = new FileInputStream(propertyFile);
            this.properties = yaml.load(fis);
        } catch (FileNotFoundException e) {
            throw new MangleException(e.getMessage());
        }
    }

}
