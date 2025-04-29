import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { CartItem } from '../../shared/models/cartitem.model';

@Injectable({ providedIn: 'root' })
export class CartService {
  constructor(private http: HttpClient) { }
  list(): Observable<CartItem[]> {
    return this.http.get<CartItem[]>('/api/cart');
  }
  add(productId: number, quantity: number): Observable<CartItem> {
    // First check if product already exists in cart
    return this.list().pipe(
      switchMap(items => {
        // Find if product already exists in cart
        const existingItem = items.find(item => item.product.productId === productId);

        if (existingItem) {
          // If exists, update quantity instead of adding new item
          return this.update(existingItem.cartItemId, existingItem.quantityInCart + quantity);
        } else {
          // If doesn't exist, add new item
          return this.http.post<CartItem>('/api/cart/items', { productId, quantity });
        }
      })
    );
  }
  remove(itemId: number) {
    return this.http.delete(`/api/cart/items/${itemId}`);
  }
  update(itemId: number, quantity: number) {
    return this.http.put<CartItem>(`/api/cart/items/${itemId}`, null, { params: { quantity } });
  }

  getCartItemCount(): Observable<number> {
    return this.list().pipe(
      switchMap(items => {
        const totalCount = items.reduce((count, item) => count + item.quantityInCart, 0);
        return new Observable<number>(observer => {
          observer.next(totalCount);
          observer.complete();
        });
      })
    );
  }
}