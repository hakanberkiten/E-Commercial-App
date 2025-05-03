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
    const timestamp = new Date().getTime();
    return this.http.get<any[]>(`/api/orders/seller/${sellerId}?_=${timestamp}`);
  }

  getOrdersByUserId(userId: number): Observable<any[]> {
    const timestamp = new Date().getTime();
    return this.http.get<any[]>(`/api/orders/user/${userId}?_=${timestamp}`);
  }

  getOrderById(orderId: number, forceRefresh: boolean = false): Observable<any> {
    // Önbellek sorunlarını önlemek için
    const timestamp = new Date().getTime();
    const url = `/api/orders/${orderId}?_=${timestamp}&forceRefresh=${forceRefresh}`;

    const headers = new HttpHeaders({
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0'
    });

    return this.http.get<any>(url, { headers }).pipe(
      tap(response => {
        // Gelen yanıtta orderStatus doğru alanın adı mı kontrol et
        const status = response.orderStatus || response.status;
        console.log(`Order ${orderId} details retrieved:`, response);
        console.log(`Order ${orderId} status from DB: ${status}`);
      }),
      catchError(error => {
        console.error('Error retrieving order details:', error);
        return throwError(() => new Error('Failed to load order details.'));
      })
    );
  }

  updateOrderStatus(orderId: number, status: string): Observable<any> {
    // Get the authentication token
    const token = localStorage.getItem('jwt_token');

    // Create headers with authorization
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Use POST instead of PATCH since we know POST works for other endpointsg
    return this.http.post(`/api/orders/${orderId}/status-update`, { status }, { headers })
      .pipe(
        tap(response => {
          console.log(`Order ${orderId} status updated to ${status}:`, response);
        })
      );
  }
  returnOrder(orderId: number): Observable<any> {
    return this.http.post(`/api/orders/${orderId}/refund`,{});
  }

  refundAndCancelOrder(orderId: number, cancelledBy?: string): Observable<any> {
    const url = cancelledBy ?
      `/api/orders/${orderId}/refund?cancelledBy=${cancelledBy}` :
      `/api/orders/${orderId}/refund`;

    return this.http.post(url, {}).pipe(
      tap(response => {
        console.log('Order refunded and cancelled successfully:', response);
      }),
      catchError(error => {
        console.error('Error refunding order:', error);
        return throwError(() => new Error(error.error?.message || 'Failed to cancel order'));
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

  cancelSellerItems(orderId: number): Observable<any> {
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser?.userId;

    if (!sellerId) {
      console.error('Error: Cannot cancel items. Seller ID not found');
      return throwError(() => new Error('Seller ID not available'));
    }

    return this.http.post(`/api/orders/${orderId}/cancel-seller-items`, {
      sellerId: sellerId
    }).pipe(
      tap(response => {
        console.log('Seller items cancelled successfully:', response);
      }),
      catchError(error => {
        console.error('Error cancelling seller items:', error);
        return throwError(() => new Error(error.error?.message || 'Failed to cancel items'));
      })
    );
  }
}
