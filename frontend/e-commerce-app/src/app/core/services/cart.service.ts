import { HttpClient } from '@angular/common/http';
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { CartItem } from '../../shared/models/cartitem.model';

@Injectable({ providedIn: 'root' })
export class CartService {
  private cartItemCountSubject = new BehaviorSubject<number>(0);
  public cartItemCount$ = this.cartItemCountSubject.asObservable();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.refreshCartCount();
  }

  private refreshCartCount(): void {
    this.list().subscribe(items => {
      const distinctProductIds = new Set();
      items.forEach(item => {
        distinctProductIds.add(item.product.productId);
      });
      this.cartItemCountSubject.next(distinctProductIds.size);
    });
  }

  list(): Observable<CartItem[]> {
    if (!this.isBrowser) {
      return of([]);
    }

    return this.http.get<CartItem[]>('/api/cart').pipe(
      catchError(error => {
        console.error('Error fetching cart:', error);
        return of([]);
      })
    );
  }

  add(productId: number, quantity: number): Observable<CartItem> {
    // Check authentication first
    if (!localStorage.getItem('jwt_token')) {
      // Return an error Observable with a friendly message
      return throwError(() => ({
        status: 401,
        message: 'Please login to add items to your cart'
      }));
    }

    return this.list().pipe(
      switchMap(items => {
        const existingItem = items.find(item => item.product.productId === productId);

        if (existingItem) {
          return this.update(existingItem.cartItemId, existingItem.quantityInCart + quantity);
        } else {
          return this.http.post<CartItem>('/api/cart/items', { productId, quantity })
            .pipe(
              tap(() => this.refreshCartCount()),
              catchError(error => {
                // Handle HTTP errors with better messages
                if (error.status === 403 || error.status === 401) {
                  return throwError(() => ({
                    status: error.status,
                    message: 'Please login to add items to your cart'
                  }));
                }
                return throwError(() => error);
              })
            );
        }
      })
    );
  }

  remove(itemId: number): Observable<any> {
    return this.http.delete(`/api/cart/items/${itemId}`)
      .pipe(
        tap(() => this.refreshCartCount())
      );
  }

  update(itemId: number, quantity: number): Observable<CartItem> {
    return this.http.put<CartItem>(`/api/cart/items/${itemId}`, null, { params: { quantity } })
      .pipe(
        tap(() => this.refreshCartCount())
      );
  }

  getCartItemCount(): Observable<number> {
    return this.cartItemCount$;
  }

  placeOrder(orderRequest: any): Observable<any> {
    return this.http.post('/api/orders/place', orderRequest);
  }
}
