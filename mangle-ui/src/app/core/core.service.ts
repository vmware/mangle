import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from '../common/service.constants';

@Injectable({
  providedIn: 'root'
})
export class CoreService {

  constructor(private http: HttpClient) { }

  public getMyDetails(): Observable<any> {
    return this.http.get(ServiceConstants.USER_MANAGEMENT_USER);
  }

  public getMyRolesAndPrivileges(roleQueryString): Observable<any> {
    return this.http.get(ServiceConstants.ROLE_MANAGEMENT_ROLES + '?' + roleQueryString);
  }

}
