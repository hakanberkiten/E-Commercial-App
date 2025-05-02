// src/app/features/products/product-list/product-list.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../shared/models/product.model';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  standalone: false,
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  searchQuery: string | null = null;
  error: string = '';
  successMessage: string = '';

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private cartService: CartService
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.searchQuery = params['search'] || null;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;

    if (this.searchQuery) {
      // Arama yapılıyorsa
      this.productService.getProductsBySearch(this.searchQuery).subscribe({
        next: data => {
          this.products = data;
          this.loading = false;
        },
        error: err => {
          console.error('Error loading search results:', err);
          this.loading = false;
        }
      });
    } else {
      // Normal ürün listesi
      this.productService.getAll().subscribe({
        next: data => {
          this.products = data;
          this.loading = false;
        },
        error: err => {
          console.error('Error loading products:', err);
          this.loading = false;
        }
      });
    }
  }

  addToCart(product: Product): void {
    if (product.quantityInStock <= 0) {
      this.error = `Sorry, "${product.productName}" is currently out of stock.`;
      setTimeout(() => this.error = '', 3000);
      return;
    }

    this.cartService.add(product.productId, 1).subscribe({
      next: () => {
        this.successMessage = `${product.productName} added to your cart!`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.error = error.message || 'Failed to add item to cart.';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }
}
