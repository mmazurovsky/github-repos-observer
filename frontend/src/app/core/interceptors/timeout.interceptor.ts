import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, TimeoutError, catchError, throwError, timeout } from 'rxjs';

@Injectable()
export class TimeoutInterceptor implements HttpInterceptor {
  private readonly DEFAULT_TIMEOUT = 120000; // 2 minutes for all requests

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      timeout(this.DEFAULT_TIMEOUT),
      catchError(error => {
        if (error instanceof TimeoutError) {
          console.error('Request timed out:', req.url);
          return throwError(() => new Error('Request timed out. Please try again.'));
        }
        return throwError(() => error);
      })
    );
  }
}
