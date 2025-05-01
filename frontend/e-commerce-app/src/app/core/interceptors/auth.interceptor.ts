import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpResponse,
    HttpErrorResponse
} from '@angular/common/http';
import { Observable, tap, throwError, catchError } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private authService: AuthService,
        @Inject(PLATFORM_ID) private platformId: Object
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Get the auth token
        const token = this.authService.getToken();

        console.log('Intercepting request to:', request.url);
        console.log('Token present:', !!token);

        // Clone the request and add the authorization header
        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
            console.log('Request with auth header:', request.headers.has('Authorization'));
        }

        // Pass the cloned request instead of the original request
        return next.handle(request);
    }

}
