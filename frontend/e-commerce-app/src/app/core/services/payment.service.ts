import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  constructor(private http: HttpClient) { }

  // Create a Stripe customer for the user
  createStripeCustomer(userId: number): Observable<string> {
    return this.http.post(`/api/payments/stripe/customers/${userId}`, {}, {
      responseType: 'text'
    });
  }

  // Add a payment method (card) to a customer
  addCard(userId: number, cardData: any): Observable<string> {
    // Add explicit content type
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(
      `/api/payments/stripe/customers/${userId}/cards`,
      cardData,
      {
        headers: headers,
        responseType: 'text'
      }
    );
  }

  // Add a payment method token to a customer
  addCardToken(userId: number, paymentMethodId: string): Observable<string> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(
      `/api/payments/stripe/customers/${userId}/payment-methods`,
      { paymentMethodId: paymentMethodId },
      {
        headers: headers,
        responseType: 'text'
      }
    );
  }

  // Get all cards for a customer
  getUserCards(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/payments/stripe/customers/${userId}/cards`);
  }

  // Process a payment
  processPayment(paymentData: any): Observable<any> {
    return this.http.post<any>('/api/payments/process', paymentData);
  }

  // Set default payment method for a customer
  setDefaultPaymentMethod(userId: number, paymentMethodId: string): Observable<any> {
    return this.http.post(
      `/api/payments/stripe/customers/${userId}/payment-methods/default`,
      { paymentMethodId }
    );
  }

  completeOrder(orderData: {
    userId: number;
    paymentMethodId: string;
    items: Array<{ productId: number; quantity: number }>;
  }): Observable<any> {
    // Get the auth token directly
    const token = localStorage.getItem('jwt_token');

    // Create headers with both content type and auth token
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // This will trigger the backend process that:
    // 1. Creates the order in the database
    // 2. Processes the payment
    // 3. Sends notifications to the seller
    return this.http.post<any>(
      '/api/orders/place',
      orderData,
      { headers }
    ).pipe(
      tap(response => {
        console.log('Order completed successfully:', response);
      })
    );
  }
}
