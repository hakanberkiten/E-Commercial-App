import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  getOrdersBySellerId(sellerId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/orders/seller/${sellerId}`);
  }

  getOrdersByUserId(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/orders/user/${userId}`);
    // No date manipulation here - just return the raw data from the backend
  }

  updateOrderStatus(orderId: number, status: string): Observable<any> {
    // Get the authentication token
    const token = localStorage.getItem('jwt_token');

    // Create headers with authorization
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Use POST instead of PATCH since we know POST works for other endpoints
    return this.http.post(`/api/orders/${orderId}/status-update`, { status }, { headers })
      .pipe(
        tap(response => {
          console.log(`Order ${orderId} status updated to ${status}:`, response);
        })
      );
  }

  refundAndCancelOrder(orderId: number): Observable<any> {
    return this.http.post(`/api/orders/${orderId}/refund`, {}).pipe(
      tap(response => {
        console.log('Order refunded and cancelled successfully:', response);
      })
    );
  }

  approveSellerItems(orderId: number): Observable<any> {
    // Get the current seller ID from the auth service
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser?.userId;

    if (!sellerId) {
      console.error('Error: Cannot approve items. Seller ID not found');
      throw new Error('Seller ID not available');
    }

    // Get the authentication token
    const token = localStorage.getItem('jwt_token');

    // Create headers with authorization
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Include the sellerId in the request body
    return this.http.post(`/api/orders/${orderId}/approve-seller-items`, { sellerId }, { headers })
      .pipe(
        tap(response => {
          console.log(`Seller items approved for order ${orderId}:`, response);
        }),
        catchError(error => {
          // Special handling for missing payment method
          if (error.error?.code === 'NO_PAYMENT_METHOD') {
            console.error('Missing payment method:', error.error?.error);
            return throwError(() => ({
              error: { message: error.error?.error || 'You need to add a payment method in your profile before approving orders' },
              noPaymentMethod: true
            }));
          }

          console.error(`Error approving items for order ${orderId}:`, error);
          throw error;
        })
      );
  }
}
