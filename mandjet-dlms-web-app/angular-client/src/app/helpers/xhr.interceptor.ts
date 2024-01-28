import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor, HTTP_INTERCEPTORS
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class XhrInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    let authReq = req.clone({ headers: req.headers.set('X-Requested-With', 'XMLHttpRequest') });
    return next.handle(authReq);
  }
}

export const xhrInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: XhrInterceptor, multi: true }
];
