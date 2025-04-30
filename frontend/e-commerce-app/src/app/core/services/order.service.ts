import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(private http: HttpClient) { }

  getOrdersBySellerId(sellerId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/orders/seller/${sellerId}`);
  }

  updateOrderStatus(orderId: number, status: string): Observable<any> {
    return this.http.patch<any>(`/api/orders/${orderId}/status`, { status });
  }
}