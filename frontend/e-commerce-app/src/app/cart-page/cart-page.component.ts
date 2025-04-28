// src/app/cart-page/cart-page.component.ts
import { Component, OnInit } from '@angular/core';
import { CartService } from '../core/services/cart.service';
import { CartItem } from '../shared/models/cartitem.model';

@Component({
  selector: 'app-cart-page',
  standalone: false,
  templateUrl: './cart-page.component.html'
})
export class CartPageComponent implements OnInit {
  items: CartItem[] = [];
  error = '';

  constructor(private cartSvc: CartService) { }

  ngOnInit() { this.load(); }

  load() {
    this.cartSvc.list()
      .subscribe(i => this.items = i, e => this.error = e);
  }

  remove(item: CartItem) {
    this.cartSvc.remove(item.cartItemId)
      .subscribe(() => this.load(), e => this.error = e);
  }

  update(item: CartItem, qty: string) {
    const q = +qty;
    if (q > 0)
      this.cartSvc.update(item.cartItemId, q)
        .subscribe(() => this.load(), e => this.error = e);
  }

  get total() {
    return this.items
      .map(i => i.product.price * i.quantityInCart)
      .reduce((a, b) => a + b, 0);
  }
}
