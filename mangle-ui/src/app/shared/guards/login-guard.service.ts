import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import {catchError, map} from 'rxjs/operators';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { ServiceConstants } from '../../common/service.constants';

@Injectable()
export class LoginGuardService implements CanActivate {

  constructor(private router: Router, private http: HttpClient) {
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.http.post(ServiceConstants.USER_MANAGEMENT_USERS_LOGIN, {}).pipe(map(
      response => {
          this.router.navigateByUrl('core');
          return false;
      }
    ),
      catchError(err => {
          return of(true);
      }));
  }

}
