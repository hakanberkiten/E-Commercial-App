import { AfterContentInit, AfterViewChecked, Component, OnInit } from '@angular/core';
import { NotificationService, Notification } from '../../services/notification.service';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-notifications',
  standalone: false,
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationsComponent implements OnInit, AfterViewChecked {
  notifications: Notification[] = [];
  loading: boolean = false;
  error: string = '';
  private clickedNotificationIds: Set<number> = new Set<number>();

  constructor(
    private notificationService: NotificationService,
    private router: Router,
    private http: HttpClient,
    private orderService: OrderService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngAfterViewChecked(): void {
    // Check for highlighted notification from query params with increased delay
    this.route.queryParams.subscribe(params => {
      if (params['highlight'] && !this.clickedNotificationIds.has(Number(params['highlight']))) {
        // Only highlight if not clicked yet
        this.scrollToAndHighlightNotification(Number(params['highlight']));
      }
    });
  }

  markAsRead(notificationId: number): void {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification && !notification.seen) {
      this.notificationService.markAsSeen(notificationId).subscribe({
        next: () => {
          notification.seen = true;
        },
        error: (error) => console.error('Error marking notification as read:', error)
      });
    }
  }

  hasUnreadNotifications(): boolean {
    return this.notifications?.some(notification => !notification.seen);
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getAllNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load notifications. Please try again.';
        this.loading = false;
        console.error('Error loading notifications:', error);
      }
    });
  }

  markAsSeen(notification: Notification): void {
    // Add notification ID to "clicked" set temporarily
    this.clickedNotificationIds.add(notification.id);

    // Remove highlight class if it exists
    const element = document.getElementById(`notification-${notification.id}`);
    if (element) {
      element.classList.remove('notification-highlight');
    }

    // Original marking as seen logic
    if (!notification.seen) {
      this.notificationService.markAsSeen(notification.id).subscribe({
        next: () => {
          notification.seen = true;
        },
        error: (error) => console.error('Error marking notification as seen:', error)
      });
    }
  }

  markAllAsSeen(): void {
    this.notificationService.markAllAsSeen().subscribe({
      next: () => {
        this.notifications.forEach(n => n.seen = true);
      },
      error: (error) => console.error('Error marking all notifications as seen:', error)
    });
  }

  deleteNotification(notification: Notification): void {
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
      },
      error: (error) => console.error('Error deleting notification:', error)
    });
  }

  goToNotification(notification: Notification): void {
    if (!notification.seen) {
      this.markAsSeen(notification);
    }

    if (notification.link) {
      this.router.navigateByUrl(notification.link);
    }
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ORDER':
        return 'bi-bag-check';
      case 'ORDER_SHIPPED':
      case 'ORDER_PARTIAL_SHIPPED':
        return 'bi-truck';
      case 'ORDER_DELIVERED':
        return 'bi-check2-circle';
      case 'ORDER_CANCELLED':
      case 'ORDER_CANCELLED_BY_SELLER':
        return 'bi-x-circle';
      case 'EARNINGS':
        return 'bi-cash';
      case 'EARNINGS_DEDUCTION':
        return 'bi-cash-coin';
      case 'SELLER_REQUEST':
        return 'bi-shop';
      default:
        return 'bi-bell';
    }
  }

  formatNotificationTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  approveSellerRequest(notification: Notification, event: Event) {
    event.stopPropagation(); // Prevent notification click event

    if (confirm("Are you sure you want to approve this user as a seller?")) {
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
          // Remove from current list and update visual feedback
          target.innerHTML = '<i class="bi bi-check-circle-fill fs-4"></i>';
          target.classList.add('btn-success');
          target.classList.remove('btn-outline-success');

          setTimeout(() => {
            this.notificationService.deleteNotification(notification.id).subscribe({
              next: () => {
                this.notifications = this.notifications.filter(n => n.id !== notification.id);
                this.showToast('Success', 'Seller request approved successfully', 'success');
                // Clear any pending requests for this user (local user)
                this.notificationService.clearSellerRequestPending();
              }
            });
          }, 1000);
        },
        error: (error) => {
          console.error('Error approving seller request:', error);
          target.disabled = false;
          target.innerHTML = originalHTML;
          this.showToast('Error', 'Failed to approve seller request', 'danger');
        }
      });
    }
  }

  denySellerRequest(notification: Notification, event: Event) {
    event.stopPropagation(); // Prevent notification click event

    if (confirm("Are you sure you want to deny this seller request?")) {
      // Visual feedback - show loading state
      const target = event.currentTarget as HTMLButtonElement;
      const originalHTML = target.innerHTML;
      target.disabled = true;
      target.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';

      // Just delete the notification
      this.notificationService.deleteNotification(notification.id).subscribe({
        next: () => {
          // Visual feedback before removal
          target.innerHTML = '<i class="bi bi-x-circle-fill fs-4"></i>';
          target.classList.add('btn-danger');
          target.classList.remove('btn-outline-danger');

          setTimeout(() => {
            this.notifications = this.notifications.filter(n => n.id !== notification.id);
            this.showToast('Success', 'Seller request denied', 'info');
            // Clear any pending requests for this user (local user)
            this.notificationService.clearSellerRequestPending();
          }, 1000);
        },
        error: (error) => {
          console.error('Error denying seller request:', error);
          target.disabled = false;
          target.innerHTML = originalHTML;
          this.showToast('Error', 'Failed to deny seller request', 'danger');
        }
      });
    }
  }

  approveRefundRequest(notification: any, event: Event): void {
    const target = event.currentTarget as HTMLButtonElement;
    const originalHTML = target.innerHTML;
    target.disabled = true;
    target.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

    // Extract the order ID from the link
    const match = notification.link.match(/\/admin\/refund-requests\/(\d+)/);
    if (!match) {
      console.error('Could not extract order ID from notification link');
      target.disabled = false;
      target.innerHTML = originalHTML;
      return;
    }

    const orderId = parseInt(match[1]);

    this.orderService.approveRefund(orderId).subscribe({
      next: () => {
        // Visual feedback before removal
        target.innerHTML = '<i class="bi bi-check-circle-fill fs-4"></i>';
        target.classList.add('btn-success');
        target.classList.remove('btn-outline-success');

        // Delete the notification and remove from list after a short delay
        setTimeout(() => {
          this.notificationService.deleteNotification(notification.id).subscribe({
            next: () => {
              this.notifications = this.notifications.filter(n => n.id !== notification.id);
              this.showToast('Success', 'Refund request approved', 'success');
            },
            error: (err) => {
              console.error('Error deleting notification:', err);
              this.showToast('Success', 'Refund approved but notification removal failed', 'info');
            }
          });
        }, 800);
      },
      error: (error) => {
        console.error('Error approving refund request:', error);
        target.disabled = false;
        target.innerHTML = originalHTML;
        this.showToast('Error', 'Failed to approve refund request', 'danger');
      }
    });
  }

  denyRefundRequest(notification: any, event: Event): void {
    const target = event.currentTarget as HTMLButtonElement;
    const originalHTML = target.innerHTML;
    target.disabled = true;
    target.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

    // Extract the order ID from the link
    const match = notification.link.match(/\/admin\/refund-requests\/(\d+)/);
    if (!match) {
      console.error('Could not extract order ID from notification link');
      target.disabled = false;
      target.innerHTML = originalHTML;
      return;
    }

    const orderId = parseInt(match[1]);

    // Show a prompt for the rejection reason
    const reason = prompt('Please provide a reason for denying this refund request');
    if (!reason) {
      target.disabled = false;
      target.innerHTML = originalHTML;
      return;
    }

    this.orderService.denyRefund(orderId, reason).subscribe({
      next: () => {
        // Visual feedback before removal
        target.innerHTML = '<i class="bi bi-x-circle-fill fs-4"></i>';
        target.classList.add('btn-danger');
        target.classList.remove('btn-outline-danger');

        // Delete the notification and remove from list after a short delay
        setTimeout(() => {
          this.notificationService.deleteNotification(notification.id).subscribe({
            next: () => {
              this.notifications = this.notifications.filter(n => n.id !== notification.id);
              this.showToast('Info', 'Refund request denied', 'info');
            },
            error: (err) => {
              console.error('Error deleting notification:', err);
              this.showToast('Info', 'Refund denied but notification removal failed', 'info');
            }
          });
        }, 800);
      },
      error: (error) => {
        console.error('Error denying refund request:', error);
        target.disabled = false;
        target.innerHTML = originalHTML;
        this.showToast('Error', 'Failed to deny refund request', 'danger');
      }
    });
  }

  showToast(title: string, message: string, type: 'success' | 'danger' | 'info'): void {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
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

  scrollToAndHighlightNotification(notificationId: number): void {
    // Don't highlight if it's in our "clicked" set
    if (this.clickedNotificationIds.has(notificationId)) {
      this.clickedNotificationIds.delete(notificationId);
      return;
    }

    // Allow more time for the DOM to fully render
    const element = document.getElementById(`notification-${notificationId}`);

    if (element) {
      // First scroll to make sure element is visible
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });

      // Force browser to recalculate layout before animation
      window.getComputedStyle(element).opacity;

      // Add highlight class for animation - will stay highlighted
      element.classList.add('notification-highlight');
    } else {
      console.error('Notification element not found:', notificationId);
    }
  }
}
