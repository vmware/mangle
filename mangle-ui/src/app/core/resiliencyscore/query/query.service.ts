import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from 'src/app/common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
  providedIn: 'root'
})
export class QueryService {

  constructor(private http: HttpClient) { }

  public getAllQueries(): Observable<any> {
    return this.http.get(ServiceConstants.QUERIES);
  }

  public addQuery(queryConfig: any): Observable<any> {
    return this.http.post(ServiceConstants.QUERIES, queryConfig);
  }

  public updateQuery(queryConfig: any): Observable<any> {
    return this.http.put(ServiceConstants.QUERIES, queryConfig);
  }

  public deleteQuery(name: any): Observable<any> {
    return this.http.delete(ServiceConstants.QUERY_DELETE
      + CommonConstants.QUESTION_MARK + CommonConstants.QUERY_NAME + CommonConstants.EQUALS_TO + name);
  }
}