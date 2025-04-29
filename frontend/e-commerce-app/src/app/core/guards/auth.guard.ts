import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard {

    constructor(private auth: AuthService, private router: Router) { }

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): Observable<boolean> | Promise<boolean> | boolean {
        if (!this.auth.isLoggedIn()) {
            this.router.navigate(['/login']);
            return false;
        }

        // Rol kontrolü yapılacaksa
        const requiredRole = route.data['role'] as string;
        if (requiredRole && !this.auth.hasRole(requiredRole)) {
            // Kullanıcı rolüne göre uygun sayfaya yönlendir
            const userRole = this.auth.getUserRole();

            if (userRole === 'ROLE_ADMIN') {
                this.router.navigate(['/admin']);
            } else if (userRole === 'ROLE_SELLER') {
                this.router.navigate(['/seller']);
            } else if (userRole === 'ROLE_CUSTOMER') {
                this.router.navigate(['/customer']);
            } else {
                this.router.navigate(['/login']);
            }

            return false;
        }

        return true;
    }
}