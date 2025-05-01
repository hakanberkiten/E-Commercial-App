import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  constructor(private http: HttpClient) { }

  // Create a Stripe customer for the user
  createStripeCustomer(userId: number): Observable<string> {
    return this.http.post<string>(`/api/payments/stripe/customers/${userId}`, {});
  }

  // Add a payment method (card) to a customer
  addCard(userId: number, cardData: any): Observable<string> {
    return this.http.post<string>(`/api/payments/stripe/customers/${userId}/cards`, cardData);
  }

  // Get all cards for a customer
  getUserCards(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/payments/stripe/customers/${userId}/cards`);
  }

  // Process a payment
  processPayment(paymentData: any): Observable<any> {
    return this.http.post<any>('/api/payments/process', paymentData);
  }
}