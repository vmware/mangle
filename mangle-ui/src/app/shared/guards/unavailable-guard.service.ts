import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {catchError, map} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {ServiceConstants} from "../../common/service.constants";

@Injectable()
export class UnavailableGuardService implements CanActivate {

  constructor(private router: Router, private http: HttpClient) {
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.http.get(ServiceConstants.APPLICATION_HEALTH).pipe(map(
      response => {
        this.router.navigateByUrl("core");
        return false;
      }
      ),
      catchError(err => {
        return of(true);
      }));
  }

}
