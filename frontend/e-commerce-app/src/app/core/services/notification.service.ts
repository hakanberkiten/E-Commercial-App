import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, Subject } from 'rxjs';
import { switchMap, tap, shareReplay, startWith, map } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export interface Notification {
  id: number;
  message: string;
  type: string;
  seen: boolean;
  createdAt: string;
  link?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = '/api/notifications';

  // Observable sources
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private unseenCountSubject = new BehaviorSubject<number>(0);
  private refreshNotifications = new Subject<void>();

  // Platform check for browser vs server
  private isBrowser: boolean;

  // New subject for pending seller request status - initialize with false by default
  private pendingRequestSubject = new BehaviorSubject<boolean>(false);

  // Observable streams
  notifications$ = this.notificationsSubject.asObservable();
  unseenCount$ = this.unseenCountSubject.asObservable();
  pendingRequest$ = this.pendingRequestSubject.asObservable();

  // Poll interval in ms (30 seconds)
  private pollInterval = 30000;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    // Initialize pendingRequest after browser check
    if (this.isBrowser) {
      this.pendingRequestSubject.next(this.checkLocalPendingRequest());
    }

    this.initNotificationPolling();
  }

  private checkLocalPendingRequest(): boolean {
    if (!this.isBrowser) {
      return false;
    }
    return localStorage.getItem('pendingSellerRequest') === 'true';
  }

  private initNotificationPolling(): void {
    // Only set up polling in browser environment
    if (!this.isBrowser) {
      return;
    }

    // Combine polling with manual refresh trigger
    this.refreshNotifications.pipe(
      startWith(null), // Start immediately
      switchMap(() => this.fetchRecentNotifications())
    ).subscribe();

    // Also set up regular polling
    interval(this.pollInterval)
      .subscribe(() => this.refreshNotificationsNow());
  }

  // Force refresh notifications
  refreshNotificationsNow(): void {
    this.refreshNotifications.next();
  }

  private fetchRecentNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/recent`)
      .pipe(
        map(notifications => this.filterHiddenNotifications(notifications)),
        tap(notifications => {
          this.notificationsSubject.next(notifications);
          const unseenCount = notifications.filter(n => !n.seen).length;
          this.unseenCountSubject.next(unseenCount);
        }),
        shareReplay(1)
      );
  }

  getRecentNotifications(): Observable<Notification[]> {
    return this.notifications$;
  }

  getAllNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl)
      .pipe(
        map(notifications => this.filterHiddenNotifications(notifications)),
        tap(notifications => {
          // Keep recent notifications updated if full list is fetched
          const recentNotifications = notifications.slice(0, 5);
          this.notificationsSubject.next(recentNotifications);
          const unseenCount = notifications.filter(n => !n.seen).length;
          this.unseenCountSubject.next(unseenCount);
        })
      );
  }

  getUnseenCount(): Observable<number> {
    return this.unseenCount$;
  }

  markAsSeen(notificationId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${notificationId}/seen`, {})
      .pipe(
        tap(() => {
          // Update local state
          const currentNotifications = this.notificationsSubject.value;
          const updatedNotifications = currentNotifications.map(notification =>
            notification.id === notificationId
              ? { ...notification, seen: true }
              : notification
          );
          this.notificationsSubject.next(updatedNotifications);

          // Update unseen count
          const unseenCount = updatedNotifications.filter(n => !n.seen).length;
          this.unseenCountSubject.next(unseenCount);
        })
      );
  }

  markAllAsSeen(): Observable<any> {
    return this.http.put(`${this.apiUrl}/mark-all-seen`, {})
      .pipe(
        tap(() => {
          // Update local state
          const currentNotifications = this.notificationsSubject.value;
          const updatedNotifications = currentNotifications.map(notification =>
            ({ ...notification, seen: true })
          );
          this.notificationsSubject.next(updatedNotifications);
          this.unseenCountSubject.next(0);
        })
      );
  }

  deleteNotification(notificationId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${notificationId}`)
      .pipe(
        tap(() => {
          // Update local state by removing the deleted notification
          const currentNotifications = this.notificationsSubject.value;
          const updatedNotifications = currentNotifications.filter(
            notification => notification.id !== notificationId
          );
          this.notificationsSubject.next(updatedNotifications);

          // Update unseen count
          const unseenCount = updatedNotifications.filter(n => !n.seen).length;
          this.unseenCountSubject.next(unseenCount);

          // Refresh notifications to ensure everything is up to date
          this.refreshNotificationsNow();
        })
      );
  }

  checkPendingSellerRequest(): boolean {
    if (this.isBrowser) {
      return localStorage.getItem('pendingSellerRequest') === 'true';
    }
    return false;
  }

  markSellerRequestPending(): void {
    if (this.isBrowser) {
      localStorage.setItem('pendingSellerRequest', 'true');
    }
    this.pendingRequestSubject.next(true);
  }

  clearSellerRequestPending(): void {
    if (this.isBrowser) {
      localStorage.removeItem('pendingSellerRequest');
    }
    this.pendingRequestSubject.next(false);
  }

  // Store hidden notification IDs in localStorage
  private getHiddenNotificationIds(): number[] {
    if (this.isBrowser) {
      const hiddenIds = localStorage.getItem('hiddenNotificationIds');
      return hiddenIds ? JSON.parse(hiddenIds) : [];
    }
    return [];
  }

  private saveHiddenNotificationIds(ids: number[]): void {
    if (this.isBrowser) {
      localStorage.setItem('hiddenNotificationIds', JSON.stringify(ids));
    }
  }

  // Hide a notification (client-side only)
  hideNotification(notificationId: number): void {
    const hiddenIds = this.getHiddenNotificationIds();
    if (!hiddenIds.includes(notificationId)) {
      hiddenIds.push(notificationId);
      this.saveHiddenNotificationIds(hiddenIds);
    }

    // Update local state by removing the hidden notification
    const currentNotifications = this.notificationsSubject.value;
    const updatedNotifications = currentNotifications.filter(
      notification => notification.id !== notificationId
    );
    this.notificationsSubject.next(updatedNotifications);

    // Update unseen count
    const unseenCount = updatedNotifications.filter(n => !n.seen).length;
    this.unseenCountSubject.next(unseenCount);
  }

  // Hide all notifications (client-side only)
  hideAllNotifications(): void {
    const currentNotifications = this.notificationsSubject.value;
    const notificationIds = currentNotifications.map(n => n.id);

    // Add all current IDs to hidden list
    const hiddenIds = this.getHiddenNotificationIds();
    const updatedHiddenIds = [...new Set([...hiddenIds, ...notificationIds])];
    this.saveHiddenNotificationIds(updatedHiddenIds);

    // Clear local notifications list
    this.notificationsSubject.next([]);
    this.unseenCountSubject.next(0);
  }

  // Filter out hidden notifications when fetching
  private filterHiddenNotifications(notifications: Notification[]): Notification[] {
    const hiddenIds = this.getHiddenNotificationIds();
    return notifications.filter(notification => !hiddenIds.includes(notification.id));
  }
}
