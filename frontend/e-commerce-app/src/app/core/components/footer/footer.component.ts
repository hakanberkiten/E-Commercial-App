import { AfterContentInit, Component, Inject, PLATFORM_ID, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-footer',
  standalone: false,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements AfterContentInit, OnInit {
  adminUsers: any[] = [];
  loading = false;
  hasPendingRequest = false; // Add property to track pending state

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private notificationService: NotificationService, // Add this line
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    // Check if there's a pending request when the component loads
    this.hasPendingRequest = this.notificationService.checkPendingSellerRequest();

    // Use the pendingRequest$ observable for real-time updates
    this.notificationService.pendingRequest$.subscribe(isPending => {
      this.hasPendingRequest = isPending;
    });
  }

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
    // Check if user already has a pending request
    if (this.notificationService.checkPendingSellerRequest()) {
      alert('You already have a pending seller request. Please wait for an admin to review it.');
      return;
    }

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

    // Check user role
    const userRole = this.authService.getUserRole();

    if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
      alert('Admins cannot request seller access.');
      return;
    }

    if (userRole === 'ROLE_SELLER' || userRole === 'SELLER') {
      alert('You are already a seller.');
      return;
    }

    // Visual feedback - show loading state
    const requestBtn = document.getElementById('sellerRequestBtn') as HTMLButtonElement;
    if (requestBtn) {
      const originalHTML = requestBtn.innerHTML;
      requestBtn.disabled = true;
      requestBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Processing...';
    }

    // Send seller request notification
    this.http.post('/api/notifications/seller-request', {
      userId: currentUser.userId
    }).subscribe({
      next: () => {
        // Mark as pending and update UI
        this.notificationService.markSellerRequestPending();

        // Force refresh notifications to show the new request for admins immediately
        this.notificationService.refreshNotificationsNow();

        if (requestBtn) {
          requestBtn.disabled = false;
          requestBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Request Pending';
        }

        alert('Your request has been submitted. An admin will review it shortly.');
      },
      error: (error) => {
        console.error('Error submitting seller request:', error);

        if (requestBtn) {
          requestBtn.disabled = false;
          requestBtn.innerHTML = '<i class="bi bi-shop"></i> Request Seller Access';
        }

        // Handle the case where the backend also indicates there's already a pending request
        if (error.error && error.error.message && error.error.message.includes('already have a pending')) {
          this.notificationService.markSellerRequestPending();
          alert('You already have a pending seller request. Please wait for an admin to review it.');
        } else {
          alert('Failed to submit request. Please try again later.');
        }
      }
    });
  }
}
