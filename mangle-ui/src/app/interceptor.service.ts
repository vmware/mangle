import { Injectable, NgZone } from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class InterceptorService implements HttpInterceptor {

  constructor(private ngZone: NgZone, private router: Router) { }
  
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const xhr = request.clone({
            headers: request.headers.set('X-Requested-With', 'XMLHttpRequest')
        });
        //return next.handle(xhr);
        return next.handle(xhr).pipe(tap((event: HttpEvent<any>) => {
            if (event instanceof HttpResponse) {
                // do stuff with response if you want
            }
        }, (err: any) => {
            if (err instanceof HttpErrorResponse) {
                if (err.status === 401) {
                    this.ngZone.run(() => this.router.navigateByUrl('login')).then();
                    //this.router.navigateByUrl('login')
                }
            }
        }));
    }
  
}
