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
            this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
            return false;
        }

        // Check for required roles
        const requiredRoles = route.data['roles'] as string[];
        if (requiredRoles && requiredRoles.length > 0) {
            const userRole = this.auth.getUserRole();

            // Check if user has any of the required roles
            const hasRequiredRole = requiredRoles.some(role =>
                userRole === `ROLE_${role}` || userRole === role
            );

            if (!hasRequiredRole) {
                // Redirect based on user's role
                if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
                    this.router.navigate(['/admin/dashboard']);
                } else if (userRole === 'ROLE_SELLER' || userRole === 'SELLER') {
                    this.router.navigate(['/seller-dashboard']);
                } else {
                    this.router.navigate(['/products']);
                }
                return false;
            }
        }

        return true;
    }
}