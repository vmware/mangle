import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from 'src/app/common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class MetricProviderService {

    constructor(private http: HttpClient) { }

    public getMetricProviders(): Observable<any> {
        return this.http.get(ServiceConstants.METRIC_PROVIDERS);
    }

    public getActiveMetricProvider(): Observable<any> {
        return this.http.get(ServiceConstants.METRIC_PROVIDERS + CommonConstants.QUESTION_MARK + CommonConstants.metricProviderByStatus + CommonConstants.EQUALS_TO + CommonConstants.active);
    }

    public addMetricProvider(metricProvider) {
        return this.http.post(ServiceConstants.METRIC_PROVIDERS, metricProvider);
    }

    public updateMetricProvider(metricProvider) {
        return this.http.put(ServiceConstants.METRIC_PROVIDERS, metricProvider);
    }

    public deleteMetricProvider(name) {
        return this.http.delete(ServiceConstants.METRIC_PROVIDERS + CommonConstants.QUESTION_MARK + CommonConstants.metricProviderName + CommonConstants.EQUALS_TO + name);
    }

    public updateMetricProviderStatus(name) {
        return this.http.post(ServiceConstants.METRIC_PROVIDERS_STATUS + CommonConstants.QUESTION_MARK + CommonConstants.metricProviderName + CommonConstants.EQUALS_TO + name, null);
    }

    public updateMetricCollectionStatus(status) {
        return this.http.post(ServiceConstants.METRIC_PROVIDERS_COLLECTION_STATUS + CommonConstants.QUESTION_MARK + CommonConstants.enableMangleMetrics + CommonConstants.EQUALS_TO + status, null);
    }

    public getMetricCollectionStatus(): Observable<any> {
        return this.http.get(ServiceConstants.METRIC_PROVIDERS_COLLECTION_STATUS);
    }

    public testConnection(metricProvider) {
        return this.http.post(ServiceConstants.METRIC_PROVIDERS_TEST_CONNECTION, metricProvider);
    }

}
