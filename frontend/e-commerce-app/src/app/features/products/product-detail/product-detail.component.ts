import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-product-detail',
  standalone: false,
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: any = {};
  reviews: any[] = [];
  reviewForm: FormGroup;
  loading = false;
  reviewLoading = false;
  error = '';
  successMessage = '';
  currentUser: any;
  quantity = 1;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private reviewService: ReviewService,
    private authService: AuthService,
    private cartService: CartService,
    private fb: FormBuilder
  ) {
    this.reviewForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      if (productId) {
        this.loadProductDetails(+productId);
        this.loadProductReviews(+productId);
      }
    });
  }

  loadProductDetails(productId: number): void {
    this.loading = true;
    this.productService.getProductById(productId).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading product details:', err);
        this.error = 'Could not load product details.';
        this.loading = false;
      }
    });
  }

  loadProductReviews(productId: number): void {
    this.reviewService.getReviewsByProduct(productId).subscribe({
      next: (data) => {
        this.reviews = data;
      },
      error: (err) => {
        console.error('Error loading reviews:', err);
      }
    });
  }

  submitReview(): void {
    if (this.reviewForm.invalid || !this.currentUser) return;

    this.reviewLoading = true;
    const reviewData = {
      content: this.reviewForm.value.content,
      user: {
        userId: this.currentUser.userId
      },
      product: {
        productId: this.product.productId
      }
    };

    this.reviewService.addReview(reviewData).subscribe({
      next: (response) => {
        this.reviewLoading = false;
        this.successMessage = 'Your review has been submitted!';
        this.reviewForm.reset();
        // Yeni yorumu ekle
        this.loadProductReviews(this.product.productId);
        
        // 3 saniye sonra success mesajını kaldır
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.reviewLoading = false;
        this.error = 'Failed to submit review. Please try again.';
        console.error('Review submission error:', error);
      }
    });
  }

  addToCart(): void {
    if (!this.currentUser) {
      this.error = 'Please login to add items to cart';
      return;
    }

    this.cartService.add(this.product.productId, this.quantity).subscribe({
      next: (response) => {
        this.successMessage = `${this.product.productName} added to your cart!`;
        
        // 3 saniye sonra success mesajını kaldır
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.error = 'Failed to add item to cart. Please try again.';
        console.error('Add to cart error:', error);
      }
    });
  }

  incrementQuantity(): void {
    if (this.quantity < this.product.quantityInStock) {
      this.quantity++;
    }
  }

  decrementQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric'
    });
  }
}