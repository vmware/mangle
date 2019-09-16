import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceConstants } from '../common/service.constants';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  constructor(private http: HttpClient) { }

  public getAppEvents(): Observable<any> {
    return this.http.get(ServiceConstants.EVENTS);
  }

}
