import { Component, OnInit } from '@angular/core';
import { NotificationService, Notification } from '../../services/notification.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notifications',
  standalone: false,
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading: boolean = false;
  error: string = '';

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadNotifications();
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
    return date.toLocaleString();
  }
}