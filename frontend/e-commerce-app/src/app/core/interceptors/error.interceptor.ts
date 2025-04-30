import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Unknown error occurred';

        if (isPlatformBrowser(this.platformId) && error.error instanceof ErrorEvent) {
          // Client-side error (browser only)
          errorMessage = `Error: ${error.error.message}`;
        } else {
          // Server-side error
          if (error.error && typeof error.error === 'object' && error.error.message) {
            errorMessage = error.error.message;
          } else {
            errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
          }

          console.error('Backend error:', {
            status: error.status,
            url: error.url,
            message: error.message,
            error: error.error
          });
        }

        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
