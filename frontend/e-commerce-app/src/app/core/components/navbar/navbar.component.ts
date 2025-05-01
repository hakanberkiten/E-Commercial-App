import { Component, OnInit, HostListener } from '@angular/core';
import { AuthService, User } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Router, NavigationEnd } from '@angular/router';
import { debounceTime, Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ThemeService } from '../../services/theme.service';
import { NotificationService, Notification } from '../../services/notification.service';

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
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    // Subscribe to router events to update navbar state on navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      // The isAuthPage() method will now use the updated router.url
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
    if (!notification.seen) {
      this.notificationService.markAsSeen(notification.id).subscribe();
    }

    // Navigate to the notification detail or related page
    if (notification.link) {
      this.router.navigateByUrl(notification.link);
    } else {
      this.router.navigate(['/notifications', notification.id]);
    }

    this.showNotifications = false;
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ORDER': return 'bi-bag-check';
      case 'ACCOUNT': return 'bi-person';
      case 'PRODUCT': return 'bi-box';
      case 'PAYMENT': return 'bi-credit-card';
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

  // Add this method after your other methods
  isAuthPage(): boolean {
    const url = this.router.url;
    return url.includes('/login') || url.includes('/signup');
  }
}