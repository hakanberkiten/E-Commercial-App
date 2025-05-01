import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

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
}