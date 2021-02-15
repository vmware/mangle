import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceConstants } from 'src/app/common/service.constants';
import { FaultService } from '../../fault/fault.service'

@Injectable({
  providedIn: 'root'
})
export class CalculateResiliencyScoreService {
  constructor(private http: HttpClient, private faultService: FaultService) { }

  public triggerResiliencyScoreCalculation(rScoreData: any): Observable<any> {
    this.faultService.removeExtraArgs(rScoreData);
    return this.http.post(ServiceConstants.CALCULATE_RESILIENCY_SCORE, rScoreData);
  }
}