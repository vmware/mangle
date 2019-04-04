import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient) { }

  public setConfigStatus() {
    return this.http.post('/mangle-services/rest/api/v1/authentication-management/users/admin', null);
  }

}
