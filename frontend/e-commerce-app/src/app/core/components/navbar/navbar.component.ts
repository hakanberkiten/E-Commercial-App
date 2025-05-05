import { Component, OnInit, HostListener } from '@angular/core';
import { AuthService, User } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Router, NavigationEnd } from '@angular/router';
import { debounceTime, Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ThemeService } from '../../services/theme.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { HttpClient } from '@angular/common/http';

interface SearchResult {
  id: number;
  name: string;
  price: number;
  imageUrl?: string;
}

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  searchQuery: string = '';
  searchResults: SearchResult[] = [];
  showSearchResults: boolean = false;
  private searchSubject = new Subject<string>();
  searchLoading: boolean = false;
  cartItemCount: number = 0;
  isDarkMode: boolean = false;

  notifications: Notification[] = [];
  unseenNotificationsCount: number = 0;
  showNotifications: boolean = false;
  loadingNotifications: boolean = false;
  hasUnseenNotifications: boolean = false;

  constructor(
    private auth: AuthService,
    private productService: ProductService,
    private router: Router,
    private cartService: CartService,
    private themeService: ThemeService,
    private notificationService: NotificationService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    // Subscribe to router events to update navbar state on navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      // Check if on auth page and user is logged in
      if (this.isAuthPage() && this.auth.isLoggedIn()) {
        // Log the user out when they navigate to login/signup page
        this.auth.logout();
        // No need to redirect since they're already on a login/signup page
      }
    });

    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;

      if (user) {
        this.cartService.cartItemCount$.subscribe(count => {
          this.cartItemCount = count;
        });

        // Subscribe to notifications stream instead of manually loading
        this.notificationService.notifications$.subscribe(notifications => {
          this.notifications = notifications;
          this.loadingNotifications = false;
        });

        // Subscribe to unseen count stream
        this.notificationService.unseenCount$.subscribe(count => {
          this.unseenNotificationsCount = count;
          this.hasUnseenNotifications = count > 0;
        });

        // Initial fetch
        this.loadNotifications();
      } else {
        this.cartItemCount = 0;
      }
    });

    // Existing search code
    this.searchSubject.pipe(
      debounceTime(300)
    ).subscribe(query => {
      this.performSearch(query);
    });

    // Subscribe to theme changes
    this.themeService.isDarkMode$.subscribe(isDark => {
      this.isDarkMode = isDark;
    });
  }

  // Arama input'u değiştiğinde
  onSearchChange(event: any): void {
    const query = event.target.value;
    this.searchSubject.next(query);
  }
  navigateToProfileTab(tabName: string): void {
    // Always navigate with query params, which makes the implementation more reliable
    this.router.navigate(['/profile'], {
      queryParams: { tab: tabName },
      queryParamsHandling: 'merge'
    });

    // Close any open dropdowns
    const dropdownMenu = document.querySelector('.dropdown-menu.show');
    if (dropdownMenu) {
      dropdownMenu.classList.remove('show');
    }
  }
  // Asıl arama fonksiyonu
  private performSearch(query: string): void {
    if (!query || query.trim() === '') {
      this.searchResults = [];
      return;
    }

    this.searchLoading = true;
    this.productService.searchProducts(query).subscribe({
      next: (results) => {
        this.searchResults = results;
        this.searchLoading = false;
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.searchLoading = false;
      }
    });
  }

  // Arama butonuna tıklandığında
  submitSearch(): void {
    if (this.searchQuery && this.searchQuery.trim() !== '') {
      this.router.navigate(['/products'], {
        queryParams: { search: this.searchQuery }
      });
      this.showSearchResults = false;
    }
  }

  // Ürün detaylarına git
  goToProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
    this.showSearchResults = false;
    this.searchQuery = '';
  }

  // Arama input odaklandığında
  onSearchFocus(): void {
    this.showSearchResults = true;
  }

  // Arama input'tan çıkıldığında
  onSearchBlur(): void {
    // Kullanıcı dropdown'a tıklarken sonuçların kapanmaması için biraz geciktirme ekleyin
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  // Diğer mevcut metodlar
  logout(): void {
    this.auth.logout();
  }

  isCustomer(): boolean {
    return this.auth.getUserRole() === 'ROLE_CUSTOMER';
  }

  isSeller(): boolean {
    return this.auth.getUserRole() === 'ROLE_SELLER';
  }

  isAdmin(): boolean {
    return this.auth.getUserRole() === 'ROLE_ADMIN';
  }

  // New method to check if user is either a customer or seller
  isCustomerOrSeller(): boolean {
    const role = this.auth.getUserRole();
    return role === 'ROLE_CUSTOMER' || role === 'ROLE_SELLER';
  }

  // Add this method to the NavbarComponent class


  toggleDropdown(event: Event) {
    event.preventDefault();
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) {
      dropdown.classList.toggle('show');
    }
  }

  // Add this method for theme toggling
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  // New methods for notifications
  loadNotifications(): void {
    this.loadingNotifications = true;
    this.notificationService.refreshNotificationsNow();
  }

  toggleNotifications(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.showNotifications = !this.showNotifications;

    // Close when clicking outside
    if (this.showNotifications) {
      setTimeout(() => {
        document.addEventListener('click', this.closeNotifications);
      }, 0);
    } else {
      document.removeEventListener('click', this.closeNotifications);
    }
  }

  closeNotifications = (event?: Event): void => {
    this.showNotifications = false;
    document.removeEventListener('click', this.closeNotifications);
  }

  markAllAsSeen(): void {
    this.notificationService.markAllAsSeen().subscribe();
  }

  goToNotification(notification: any): void {
    // Mark notification as seen if needed
    if (!notification.seen) {
      this.notificationService.markAsSeen(notification.id).subscribe();
    }

    // Close dropdown and navigate
    this.showNotifications = false;
    document.removeEventListener('click', this.closeNotifications);

    // Use navigationExtras with preserveFragment to ensure clean navigation
    this.router.navigate(['/notifications'], {
      queryParams: { highlight: notification.id },
      queryParamsHandling: 'merge'
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ORDER': return 'bi-bag-check';
      case 'ACCOUNT': return 'bi-person';
      case 'PRODUCT': return 'bi-box';
      case 'PAYMENT': return 'bi-credit-card';
      case 'SELLER_REQUEST': return 'bi-shop';
      case 'SYSTEM': return 'bi-gear';
      default: return 'bi-bell';
    }
  }

  formatNotificationTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'Just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes} ${minutes === 1 ? 'minute' : 'minutes'} ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours} ${hours === 1 ? 'hour' : 'hours'} ago`;
    } else if (diffInSeconds < 604800) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} ${days === 1 ? 'day' : 'days'} ago`;
    } else {
      return date.toLocaleDateString();
    }
  }

  // Add the approval/denial methods similar to the notification component
  approveSellerRequest(notification: any, event: Event) {
    event.stopPropagation(); // Prevent notification click event

    if (!notification.link) {
      console.error('No user ID found in notification link');
      return;
    }

    // Extract user ID from the link
    const userId = notification.link.split('/').pop();

    if (!userId) {
      console.error('Invalid user ID in notification link');
      return;
    }

    // Visual feedback - show loading state
    const target = event.currentTarget as HTMLButtonElement;
    const originalHTML = target.innerHTML;
    target.disabled = true;
    target.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';

    // Send approval to server
    this.http.put(`/api/users/${userId}/role`, { roleId: 2 }).subscribe({
      next: () => {
        // Visual feedback of success
        target.innerHTML = '<i class="bi bi-check-circle-fill"></i>';
        target.classList.add('btn-success');
        target.classList.remove('btn-outline-success');

        setTimeout(() => {
          this.notificationService.deleteNotification(notification.id).subscribe({
            next: () => {
              // Remove from current list
              this.notifications = this.notifications.filter(n => n.id !== notification.id);

              // Clear any pending requests for this user
              this.notificationService.clearSellerRequestPending();

              // Force refresh notifications
              this.notificationService.refreshNotificationsNow();

              // Show a more elegant toast notification instead of alert
              this.showToast('Success', 'Seller request approved successfully', 'success');
            }
          });
        }, 800);
      },
      error: (error) => {
        console.error('Error approving seller request:', error);
        target.disabled = false;
        target.innerHTML = originalHTML;
        this.showToast('Error', 'Failed to approve seller request', 'danger');
      }
    });
  }

  denySellerRequest(notification: any, event: Event) {
    event.stopPropagation(); // Prevent notification click event

    // Visual feedback - show loading state
    const target = event.currentTarget as HTMLButtonElement;
    const originalHTML = target.innerHTML;
    target.disabled = true;
    target.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';

    // Just delete the notification
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        // Visual feedback before removal
        target.innerHTML = '<i class="bi bi-x-circle-fill"></i>';
        target.classList.add('btn-danger');
        target.classList.remove('btn-outline-danger');

        setTimeout(() => {
          // Remove from current list
          this.notifications = this.notifications.filter(n => n.id !== notification.id);

          // Clear any pending requests for this user
          this.notificationService.clearSellerRequestPending();

          // Force refresh notifications
          this.notificationService.refreshNotificationsNow();

          // Show toast notification
          this.showToast('Info', 'Seller request denied', 'info');
        }, 800);
      },
      error: (error) => {
        console.error('Error denying seller request:', error);
        target.disabled = false;
        target.innerHTML = originalHTML;
        this.showToast('Error', 'Failed to deny seller request', 'danger');
      }
    });
  }

  // Add a toast notification method for better UI feedback
  showToast(title: string, message: string, type: 'success' | 'danger' | 'info'): void {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.navbar-toast-container');
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.className = 'navbar-toast-container position-fixed bottom-0 end-0 p-3';
      (toastContainer as HTMLElement).style.zIndex = '1050';
      document.body.appendChild(toastContainer);
    }

    // Create toast element
    const toastEl = document.createElement('div');
    toastEl.className = `toast show bg-${type} bg-opacity-10 text-${type} border-${type}`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    // Toast header
    const toastHeader = document.createElement('div');
    toastHeader.className = `toast-header bg-${type} bg-opacity-25 text-${type}`;

    // Icon based on type
    const icon = document.createElement('i');
    icon.className = type === 'success' ? 'bi bi-check-circle-fill me-2' :
      type === 'danger' ? 'bi bi-exclamation-circle-fill me-2' :
        'bi bi-info-circle-fill me-2';

    toastHeader.appendChild(icon);

    // Title
    const titleEl = document.createElement('strong');
    titleEl.className = 'me-auto';
    titleEl.textContent = title;
    toastHeader.appendChild(titleEl);

    // Close button
    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.className = 'btn-close';
    closeButton.setAttribute('data-bs-dismiss', 'toast');
    closeButton.setAttribute('aria-label', 'Close');
    closeButton.onclick = () => toastEl.remove();
    toastHeader.appendChild(closeButton);

    // Toast body
    const toastBody = document.createElement('div');
    toastBody.className = 'toast-body';
    toastBody.textContent = message;

    // Append header and body to toast
    toastEl.appendChild(toastHeader);
    toastEl.appendChild(toastBody);

    // Append toast to container
    toastContainer.appendChild(toastEl);

    // Auto-remove after 5 seconds
    setTimeout(() => {
      if (toastEl.parentNode) {
        toastEl.remove();
      }
    }, 5000);
  }

  // Add this method after your other methods
  isAuthPage(): boolean {
    const url = this.router.url;
    return url.includes('/login') || url.includes('/signup');
  }

  // Add a method to handle manual refresh
  refreshNotifications(event?: Event): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }

    this.loadingNotifications = true;
    this.notificationService.refreshNotificationsNow();

    // Add a short timeout to provide visual feedback of the refresh action
    setTimeout(() => {
      this.loadingNotifications = false;
    }, 800);
  }
}
