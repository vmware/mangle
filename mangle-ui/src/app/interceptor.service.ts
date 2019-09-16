import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from "@angular/router";

@Injectable({
    providedIn: 'root'
})
export class InterceptorService implements HttpInterceptor {

    constructor(private router: Router) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const xhr = request.clone({
            headers: request.headers.set('X-Requested-With', 'XMLHttpRequest')
        });
        return next.handle(xhr).pipe(tap((event: HttpEvent<any>) => {
            if (event instanceof HttpResponse) {
                event.headers.delete('Authorization');
            }
        }, (err: any) => {
            if (err instanceof HttpErrorResponse) {
                err.headers.delete('Authorization');
                if (err.status === 401) {
                    this.router.navigateByUrl('login');
                }
                if (err.status === 504) {
                    this.router.navigateByUrl('unavailable');
                }
                if (err.status === 0) {
                    window.location.reload();
                }
            }
        }));
    }

}
