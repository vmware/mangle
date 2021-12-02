import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceConstants } from '../common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  constructor(private http: HttpClient) { }

  public getAppEvents(): Observable<any> {
    return this.http.get(ServiceConstants.EVENTS);
  }
  public getActiveMetricProvider(): Observable<any> {
    return this.http.get(ServiceConstants.METRIC_PROVIDERS + CommonConstants.QUESTION_MARK + CommonConstants.metricProviderByStatus + CommonConstants.EQUALS_TO + CommonConstants.active);
}

}
