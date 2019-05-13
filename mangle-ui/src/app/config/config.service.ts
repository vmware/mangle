import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ServiceConstants } from '../common/service.constants';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient) { }

  public updateLocalUserConfig(user, oldPassword): Observable<any> {
    return this.http.put(ServiceConstants.USER_MANAGEMENT_USERS_ADMIN, user, { observe: 'response', headers: new HttpHeaders().set('Content-Type', 'application/json').set('Authorization', 'Basic ' + btoa(user.name + ':' + oldPassword)) });
  }

}
