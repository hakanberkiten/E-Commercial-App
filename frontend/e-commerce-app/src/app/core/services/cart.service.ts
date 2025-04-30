import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, switchMap, tap } from 'rxjs';
import { CartItem } from '../../shared/models/cartitem.model';

@Injectable({ providedIn: 'root' })
export class CartService {
  // Add a BehaviorSubject to track cart items count
  private cartItemCountSubject = new BehaviorSubject<number>(0);
  // Expose it as an observable for components to subscribe to
  public cartItemCount$ = this.cartItemCountSubject.asObservable();

  constructor(private http: HttpClient) {
    // Initial load of cart count
    this.refreshCartCount();
  }

  // Method to refresh the cart count
  private refreshCartCount(): void {
    this.list().subscribe(items => {
      // Count unique products
      const distinctProductIds = new Set();
      items.forEach(item => {
        distinctProductIds.add(item.product.productId);
      });
      this.cartItemCountSubject.next(distinctProductIds.size);
    });
  }

  list(): Observable<CartItem[]> {
    return this.http.get<CartItem[]>('/api/cart');
  }

  add(productId: number, quantity: number): Observable<CartItem> {
    return this.list().pipe(
      switchMap(items => {
        const existingItem = items.find(item => item.product.productId === productId);

        if (existingItem) {
          return this.update(existingItem.cartItemId, existingItem.quantityInCart + quantity);
        } else {
          return this.http.post<CartItem>('/api/cart/items', { productId, quantity })
            .pipe(
              tap(() => this.refreshCartCount()) // Update count after adding
            );
        }
      })
    );
  }

  remove(itemId: number): Observable<any> {
    return this.http.delete(`/api/cart/items/${itemId}`)
      .pipe(
        tap(() => this.refreshCartCount()) // Update count after removing
      );
  }

  update(itemId: number, quantity: number): Observable<CartItem> {
    return this.http.put<CartItem>(`/api/cart/items/${itemId}`, null, { params: { quantity } })
      .pipe(
        tap(() => this.refreshCartCount()) // Update count after updating
      );
  }

  getCartItemCount(): Observable<number> {
    return this.cartItemCount$;
  }
}