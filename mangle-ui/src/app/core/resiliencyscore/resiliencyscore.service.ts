import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from 'src/app/common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class ResiliencyscoreService {

    constructor(private http: HttpClient) { }

    public getAllServices(): Observable<any>{
        return this.http.get(ServiceConstants.SERVICES);
    }
    public addService(serviceConfig: any): Observable<any> {
        return this.http.post(ServiceConstants.SERVICES, serviceConfig);
      }
    
      public updateService(serviceConfig: any): Observable<any> {
        return this.http.put(ServiceConstants.SERVICES, serviceConfig);
      }
    
      public deleteService(name: any): Observable<any> {
        return this.http.delete(ServiceConstants.SERVICE_DELETE + CommonConstants.EQUALS_TO + name);
      }
}