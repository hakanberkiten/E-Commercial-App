import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CartItem } from '../../shared/models/cartitem.model';

@Injectable({ providedIn: 'root' })
export class CartService {
  constructor(private http: HttpClient) { }
  list(): Observable<CartItem[]> {
    return this.http.get<CartItem[]>('/api/cart');
  }
  add(productId: number, quantity: number) {
    return this.http.post<CartItem>('/api/cart/items', { productId, quantity });
  }
  remove(itemId: number) {
    return this.http.delete(`/api/cart/items/${itemId}`);
  }
  update(itemId: number, quantity: number) {
    return this.http.put<CartItem>(`/api/cart/items/${itemId}`, null, { params: { quantity } });
  }

}