import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(private http: HttpClient) { }

  getOrdersBySellerId(sellerId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/orders/seller/${sellerId}`);
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
    // Get the authentication token
    const token = localStorage.getItem('jwt_token');

    // Create headers with authorization
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.post(`/api/orders/${orderId}/approve-seller-items`, {}, { headers })
      .pipe(
        tap(response => {
          console.log(`Seller items approved for order ${orderId}:`, response);
        })
      );
  }
}