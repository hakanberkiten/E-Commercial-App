import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../core/services/category.service';
import { ProductService } from '../core/services/product.service';
import { CartService } from '../core/services/cart.service';
import { Category } from '../shared/models/category.model';
import { Product } from '../shared/models/product.model';
import { AuthService, User } from '../core/services/auth.service';
import { Router } from '@angular/router';

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
  user?: User | null;
  successMessage: string = "";
  cartItemCount: number = 0;
  loading: boolean = false;
  minPrice: number = 0;
  maxPrice: number = 10000; // M
  sortOption: string = 'default'; // Add this property to your class

  constructor(
    private catSvc: CategoryService,
    private prodSvc: ProductService,
    private cartSvc: CartService,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.catSvc.getAll().subscribe(c => this.categories = c, e => this.error = e);
    this.user = this.auth.getCurrentUser();
    this.load();
    this.loadCartCount();
  }

  load() {
    // Load categories if not already loaded
    if (this.categories.length === 0) {
      this.catSvc.getAll().subscribe({
        next: (cats) => this.categories = cats,
        error: (e) => this.error = e.message
      });
    }

    // Load products based on selected category
    this.prodSvc.getByCategory(this.selectedCat).subscribe({
      next: (prods) => this.products = prods,
      error: (e) => this.error = e.message
    });
  }

  onCategoryChange(id: string) {
    this.selectedCat = id ? +id : null;
    this.load();
  }
  goToProductDetail(productId: number): void {
    this.router.navigate(['/products', productId]);
  }
  addToCart(p: Product) {
    this.cartSvc.add(p.productId, 1)
      .subscribe({
        next: (item) => {
          this.loadCartCount();

          // Check if quantity is more than 1 (existing item)
          if (item.quantityInCart > 1) {
            this.showSuccessMessage(`Added another ${p.productName} to cart (${item.quantityInCart} total)`);
          } else {
            this.showSuccessMessage(`Added ${p.productName} to cart!`);
          }
        },
        error: e => this.error = e.message
      });
  }

  // Update the applyFilters method to include sorting
  applyFilters() {
    this.loading = true;
    this.error = '';

    this.prodSvc.getFilteredProducts(
      this.selectedCat,
      this.minPrice,
      this.maxPrice,
      this.sortOption
    ).subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading filtered products:', err);
        this.error = 'Failed to load products. Please try again.';
        this.loading = false;
      }
    });
  }

  loadCartCount() {
    this.cartSvc.list().subscribe(items => {
      this.cartItemCount = items.length;
    });
  }

  showSuccessMessage(message: string) {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  // Customer component'e roundHalf metodunu ekleyelim

  // Yarım yıldız hesaplama
  roundHalf(value: number): number {
    return Math.ceil(value * 2) / 2;
  }
}