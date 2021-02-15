import {Injectable} from "@angular/core";
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {tap} from "rxjs/operators";
import {Router} from "@angular/router";
import {ServiceConstants} from "./common/service.constants";

@Injectable({
  providedIn: "root"
})
export class InterceptorService implements HttpInterceptor {

  constructor(private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const xhr = request.clone({
      headers: request.headers.set("X-Requested-With", "XMLHttpRequest")
    });
    return next.handle(xhr).pipe(tap((event: HttpEvent<any>) => {
      if (event instanceof HttpResponse) {
        event.headers.delete("Authorization");
      }
    }, (err: any) => {
      if (err instanceof HttpErrorResponse) {
        err.headers.delete("Authorization");
        if (err.status === 401 && !err.url.endsWith(ServiceConstants.USER_MANAGEMENT_USERS_LOGIN)) {
          this.router.navigateByUrl("login");
        }
        if (err.status === 504 && !err.url.endsWith(ServiceConstants.APPLICATION_HEALTH)) {
          this.router.navigateByUrl("unavailable");
        }
        if (err.status === 500 && err.error.code === "FISEC007") {
          this.router.navigateByUrl("config");
        }
        if (err.status === 0) {
          window.location.reload();
        }
      }
    }));
  }

}
