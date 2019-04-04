import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CoreService {

  constructor(private http: HttpClient) { }

  public getMyDetails(): Observable<any> {
    return this.http.get('/mangle-services/rest/api/v1/user-management/user');
  }

  public getMyRolesAndPrivileges(roleQueryString): Observable<any> {
    return this.http.get('/mangle-services/rest/api/v1/role-management/roles?' + roleQueryString);
  }

}
