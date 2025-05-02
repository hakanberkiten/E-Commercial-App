import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../services/category.service';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Category } from '../../../shared/models/category.model';
import { Product } from '../../../shared/models/product.model';
import { AuthService, User } from '../../services/auth.service';
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

  // Add these properties to your class
  currentPage: number = 1;
  pageSize: number = 9;
  totalPages: number = 1;

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
      next: (prods) => {
        this.products = prods;
        this.totalPages = Math.ceil(this.products.length / this.pageSize);
        this.currentPage = 1;
      },
      error: (e) => this.error = e.message
    });
  }

  onCategoryChange(categoryId: string) {
    console.log('Category changed to:', categoryId);

    // Parse the category ID (or set to null if empty)
    this.selectedCat = categoryId ? parseInt(categoryId, 10) : null;

    // Always use applySortAndFilters to ensure sort option is included
    this.applySortAndFilters();
  }
  goToProductDetail(productId: number): void {
    this.router.navigate(['/products', productId]);
  }
  addToCart(p: Product) {
    // Check stock first
    if (p.quantityInStock <= 0) {
      this.error = `Sorry, "${p.productName}" is currently out of stock.`;
      setTimeout(() => this.error = '', 3000);
      return;
    }

    const currentUser = this.auth.getCurrentUser();

    // Check if user is logged in first
    if (!currentUser) {
      this.error = `Please login to add items to your cart`;
      setTimeout(() => {
        this.error = '';
        // Optional: Redirect to login
        // this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      }, 3000);
      return;
    }

    // Check if the current user is the seller of this product
    if (currentUser && p.seller && p.seller.userId === currentUser.userId) {
      this.error = `You cannot purchase your own product "${p.productName}".`;
      setTimeout(() => this.error = '', 3000);
      return;
    }

    this.cartSvc.add(p.productId, 1)
      .subscribe({
        next: (item) => {
          this.loadCartCount();
          if (item.quantityInCart > 1) {
            this.showSuccessMessage(`Added another ${p.productName} to cart (${item.quantityInCart} total)`);
          } else {
            this.showSuccessMessage(`Added ${p.productName} to cart!`);
          }
        },
        error: (e) => {
          this.error = e.message || `Could not add "${p.productName}" to cart`;
          setTimeout(() => this.error = '', 3000);
        }
      });
  }

  // Add this method
  navigateToProductDetail(productId: number): void {
    this.router.navigate(['/products', productId]);
  }

  // Add this method to ensure filters are properly synchronized
  applySortAndFilters() {
    // Log current filter state for debugging
    console.log('Applying filters with:', {
      category: this.selectedCat,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      sortOption: this.sortOption
    });

    this.loading = true;
    this.error = '';

    // Use the current category ID even if it's null
    const categoryId = this.selectedCat;

    this.prodSvc.getFilteredProducts(
      categoryId,
      this.minPrice,
      this.maxPrice,
      this.sortOption
    ).subscribe({
      next: (products) => {
        this.products = products;
        this.totalPages = Math.ceil(this.products.length / this.pageSize);
        this.currentPage = 1; // Reset to first page when filters change
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading filtered products:', err);
        this.error = 'Failed to load products. Please try again.';
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  // Replace your current applyFilters with this
  applyFilters() {
    this.applySortAndFilters();
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
  get paginatedProducts(): Product[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.products.slice(startIndex, endIndex);
  }
  // Add these pagination methods
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      window.scrollTo(0, 0); // Scroll to top when changing pages
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.goToPage(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.goToPage(this.currentPage - 1);
    }
  }
}
