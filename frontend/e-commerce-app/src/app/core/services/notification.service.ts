import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, Subject } from 'rxjs';
import { switchMap, tap, shareReplay, startWith } from 'rxjs/operators';

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

  // Observable streams
  notifications$ = this.notificationsSubject.asObservable();
  unseenCount$ = this.unseenCountSubject.asObservable();

  // Poll interval in ms (e.g. 30 seconds)
  private pollInterval = 30000;

  constructor(private http: HttpClient) {
    this.initNotificationPolling();
  }

  private initNotificationPolling(): void {
    // Combine polling with manual refresh trigger
    interval(this.pollInterval)
      .pipe(
        startWith(0), // Start immediately
        // Also refresh when refreshNotifications is triggered
        switchMap(() => this.fetchRecentNotifications())
      )
      .subscribe();
  }

  // Force refresh notifications
  refreshNotificationsNow(): void {
    this.refreshNotifications.next();
  }

  private fetchRecentNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/recent`)
      .pipe(
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
        })
      );
  }
}