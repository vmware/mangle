import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from '../common/service.constants';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  public login(loginData): Observable<HttpResponse<any>> {
    return this.http.get<HttpResponse<any>>(ServiceConstants.USER_MANAGEMENT_USER, { observe: 'response', headers: new HttpHeaders().set('Content-Type', 'application/json').set('Authorization', 'Basic ' + btoa(loginData.username + '@' + loginData.authSource + ':' + loginData.password)) });
  }

  public logout() {
    this.http.get(ServiceConstants.LOGOUT).subscribe();
  }

  public getAuthSources(): Observable<any> {
    return this.http.get(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_DOMAINS);
  }

}
