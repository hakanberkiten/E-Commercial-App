// src/app/customer/customer.component.ts
import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../core/services/category.service';
import { ProductService } from '../core/services/product.service';
import { CartService } from '../core/services/cart.service';
import { Category } from '../shared/models/category.model';
import { Product } from '../shared/models/product.model';
import { AuthService, User } from '../core/services/auth.service';

@Component({
  selector: 'app-customer',
  standalone: false,
  templateUrl: './customer.component.html'
})
export class CustomerComponent implements OnInit {
  categories: Category[] = [];
  products: Product[] = [];
  selectedCat: number | null = null;
  error = '';
  user?: User;

  constructor(
    private catSvc: CategoryService,
    private prodSvc: ProductService,
    private cartSvc: CartService,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.catSvc.getAll().subscribe(c => this.categories = c, e => this.error = e);
    this.load();

  }

  load() {
    if (this.selectedCat)
      this.prodSvc.getByCategory(this.selectedCat)
        .subscribe(p => this.products = p, e => this.error = e);
    else
      this.prodSvc.getAll()
        .subscribe(p => this.products = p, e => this.error = e);
  }

  onCategoryChange(id: string) {
    this.selectedCat = id ? +id : null;
    this.load();
  }

  addToCart(p: Product) {
    this.cartSvc.add(p.productId, 1)
      .subscribe({
        next: _ => alert('Added!'),
        error: e => this.error = e.message
      });
  }
}
