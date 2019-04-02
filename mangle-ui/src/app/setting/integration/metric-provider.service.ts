import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class MetricProviderService {

    constructor(private http: HttpClient) { }

    public getMetricProviders(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/metric-providers');
    }

    public getActiveMetricProvider(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/metric-providers?isActiveMetricProvider=true');
    }

    public addMetricProvider(metricProvider) {
        return this.http.post('/mangle-services/rest/api/v1/metric-providers', metricProvider);
    }

    public updateMetricProvider(metricProvider) {
        return this.http.put('/mangle-services/rest/api/v1/metric-providers', metricProvider);
    }

    public deleteMetricProvider(name) {
        return this.http.delete('/mangle-services/rest/api/v1/metric-providers?metricProviderName=' + name);
    }

    public updateMetricProviderStatus(name) {
        return this.http.post('/mangle-services/rest/api/v1/metric-providers/status?metricProviderName=' + name, null);
    }

    public updateMetricCollectionStatus(status) {
        return this.http.post('/mangle-services/rest/api/v1/metric-providers/mangle-metrics-collection-status?enableMangleMetrics=' + status, null);
    }

    public testConnection(metricProvider) {
        return this.http.post('/mangle-services/rest/api/v1/metric-providers/test-connection', metricProvider);
    }

}
