import { AfterContentInit, Component, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: false,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements AfterContentInit {
  adminUsers: any[] = [];
  loading = false;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngAfterContentInit(): void {
    // Only load admin users in the browser, not during SSR
    if (isPlatformBrowser(this.platformId)) {
      this.loadAdminUsers();
    }
  }

  loadAdminUsers() {
    this.loading = true;
    this.http.get<any[]>('/api/users/admin-contacts').subscribe({
      next: (admins) => {
        this.adminUsers = admins;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading admin users:', error);
        this.loading = false;
      }
    });
  }

  requestSellerAccess() {
    // Check if user is logged in
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      // Redirect to login with return URL
      this.router.navigate(['/login'], {
        queryParams: {
          returnUrl: this.router.url,
          action: 'sellerRequest'
        }
      });
      return;
    }

    // Send seller request to backend
    this.http.post('/api/seller-requests', {
      userId: currentUser.userId
    }).subscribe({
      next: () => {
        alert('Your request has been submitted. An admin will review it shortly.');
      },
      error: (error) => {
        console.error('Error submitting seller request:', error);
        alert('Failed to submit request. Please try again later.');
      }
    });
  }
}
