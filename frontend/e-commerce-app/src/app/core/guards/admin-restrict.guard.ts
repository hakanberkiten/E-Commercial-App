import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminRestrictGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const currentUser = this.authService.getCurrentUser();
    // If user is admin and trying to access restricted paths, redirect to admin dashboard
    if (currentUser?.role?.roleName === 'ADMIN' || currentUser?.role?.roleName === 'ROLE_ADMIN') {
      // Check if the path contains 'products'
      if (state.url.includes('/products')) {
        this.router.navigate(['/admin/dashboard']);
        return false;
      }
    }
    return true;
  }
}