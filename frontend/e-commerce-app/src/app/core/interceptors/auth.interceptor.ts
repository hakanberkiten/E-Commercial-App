import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private authService: AuthService,
        @Inject(PLATFORM_ID) private platformId: Object
    ) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Sadece tarayıcı ortamında token ekle
        if (isPlatformBrowser(this.platformId)) {
            const token = this.authService.getToken();

            if (token) {
                const authReq = req.clone({
                    headers: req.headers.set('Authorization', `Bearer ${token}`)
                });
                return next.handle(authReq);
            }
        }

        return next.handle(req);
    }
}