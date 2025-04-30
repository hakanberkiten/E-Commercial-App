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
        // Get the auth token from the service
        const authToken = this.authService.getToken();

        // Clone the request and add the auth header if token exists
        if (authToken) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${authToken}`
                }
            });
        }

        // Handle response and check for token invalidation
        return next.handle(request).pipe(
            tap(event => {
                if (event instanceof HttpResponse) {
                    if (event.headers.get('X-Token-Invalid') === 'true') {
                        const reason = event.headers.get('X-Token-Invalid-Reason');

                        if (reason === 'role-changed') {
                            console.warn('Your role has changed. Please log in again for security reasons.');
                            // Force logout and redirect to login
                            if (isPlatformBrowser(this.platformId)) {
                                this.authService.logout();
                                alert('Your user role has been updated. Please log in again to continue.');
                            }
                        }
                    }
                }
            }),
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401) {
                    // Handle unauthorized error
                    if (error.error?.message === 'Account has been deactivated') {
                        if (isPlatformBrowser(this.platformId)) {
                            this.authService.logout();
                            alert('Your account has been deactivated. Please contact an administrator.');
                        }
                    } else if (error.error?.message === 'User role has changed') {
                        if (isPlatformBrowser(this.platformId)) {
                            this.authService.logout();
                            alert('Your user role has been updated. Please log in again to continue.');
                        }
                    }
                }
                return throwError(() => error);
            })
        );
    }
}
