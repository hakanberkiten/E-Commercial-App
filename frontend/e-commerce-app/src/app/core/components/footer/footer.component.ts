import { AfterContentInit, Component, Inject, PLATFORM_ID, OnInit, AfterViewInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
declare var bootstrap: any;

@Component({
  selector: 'app-footer',
  standalone: false,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements AfterContentInit, OnInit, AfterViewInit {
  adminUsers: any[] = [];
  loading = false;
  hasPendingRequest = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private notificationService: NotificationService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    // Check if there's a pending request during initialization
    this.checkPendingRequest();

    // Subscribe to auth state changes to update visibility
    this.authService.currentUser$.subscribe(() => {
      this.checkPendingRequest();
    });

    // Listen to notification service for any changes in pending status
    this.notificationService.pendingRequest$.subscribe(pending => {
      this.hasPendingRequest = pending;
    });
  }

  ngAfterContentInit(): void {
    // Only load admin users in the browser, not during SSR
    if (isPlatformBrowser(this.platformId)) {
      this.loadAdminUsers();
    }
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      // Initialize tooltips
      const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
      [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
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

  // Check if user is logged in
  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  // Check if current user is a customer (not admin, not seller)
  isCustomer(): boolean {
    const userRole = this.authService.getUserRole();
    return userRole === 'ROLE_CUSTOMER';
  }

  // Check if there's a pending request
  checkPendingRequest(): void {
    if (this.isLoggedIn() && this.isCustomer()) {
      this.hasPendingRequest = this.notificationService.checkPendingSellerRequest();
    } else {
      this.hasPendingRequest = false;
    }
  }

  requestSellerAccess(): void {
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

    // No need to check for role here since the button is only visible to customers

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
        this.hasPendingRequest = true;
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
          this.hasPendingRequest = true;
          alert('You already have a pending seller request. Please wait for an admin to review it.');
        } else {
          alert('Failed to submit request. Please try again later.');
        }
      }
    });
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role?.roleName === 'ADMIN' ||
      this.authService.currentUser?.role?.roleName === 'ROLE_ADMIN';
  }
}
