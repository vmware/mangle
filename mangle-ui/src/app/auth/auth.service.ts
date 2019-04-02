import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  public login(loginData): Observable<HttpResponse<any>> {
    return this.http.get<HttpResponse<any>>('/mangle-services/rest/api/v1/user-management/users', { observe: 'response', headers: new HttpHeaders().set('Content-Type', 'application/json').set('Authorization', 'Basic ' + btoa(loginData.username + '@' + loginData.authSource + ':' + loginData.password)) });
  }

  public logout() {
    this.http.get('/mangle-services/logout').subscribe();
  }

  public getAuthSources(): Observable<any> {
    return this.http.get('/mangle-services/rest/api/v1/auth-provider-management/domains');
  }

}
