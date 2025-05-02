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

  // Add new property
  hasUserReviewed = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private reviewService: ReviewService,
    private authService: AuthService,
    private cartService: CartService,
    private fb: FormBuilder
  ) {
    this.reviewForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(5)]],
      reviewPoint: [0, [Validators.required, Validators.min(1), Validators.max(5)]]
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
        console.log('Product data:', this.product); // Debug log
        console.log('Seller info:', this.product.seller); // Check seller details
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
      next: (reviews) => {
        this.reviews = reviews;

        // Check if current user has already reviewed this product
        if (this.currentUser) {
          this.hasUserReviewed = this.reviews.some(review =>
            review.user && review.user.userId === this.currentUser.userId
          );
        }
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
      }
    });
  }

  // Yıldız ratingi ayarlama
  setRating(rating: number): void {
    this.reviewForm.patchValue({ reviewPoint: rating });
  }

  // Yarım yıldız hesaplama
  roundHalf(value: number): number {
    return Math.ceil(value * 2) / 2;
  }

  submitReview(): void {
    if (this.reviewForm.invalid || !this.currentUser) return;

    this.reviewLoading = true;
    const reviewData = {
      content: this.reviewForm.value.content,
      reviewPoint: this.reviewForm.value.reviewPoint,
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
        this.reviewForm.reset({
          content: '',
          reviewPoint: 0
        });

        // Yeni yorumu ekle ve ürün detaylarını güncelle
        this.loadProductReviews(this.product.productId);
        this.loadProductDetails(this.product.productId);
        this.hasUserReviewed = true;

        // 3 saniye sonra success mesajını kaldır
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.reviewLoading = false;
        if (error.error && error.error.message === "You have already reviewed this product") {
          this.error = 'You have already reviewed this product.';
          this.hasUserReviewed = true;
        } else {
          this.error = 'Failed to submit review. Please try again.';
        }
        console.error('Review submission error:', error);
      }
    });
  }

  addToCart(): void {
    if (!this.currentUser) {
      this.error = 'Please login to add items to cart';
      return;
    }

    // First check if product is in stock
    if (this.product.quantityInStock <= 0) {
      this.error = 'Sorry, this product is currently out of stock.';
      // Show error for 3 seconds
      setTimeout(() => {
        this.error = '';
      }, 3000);
      return;
    }

    // Check if requested quantity exceeds available stock
    if (this.quantity > this.product.quantityInStock) {
      this.error = `Sorry, only ${this.product.quantityInStock} units available in stock.`;
      // Reset quantity to max available
      this.quantity = this.product.quantityInStock;
      setTimeout(() => {
        this.error = '';
      }, 3000);
      return;
    }

    this.cartService.add(this.product.productId, this.quantity).subscribe({
      next: (response) => {
        this.successMessage = `${this.product.productName} added to your cart!`;
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        // Display specific error message from the service
        this.error = error.message || 'Failed to add item to cart. Please try again.';
        console.error('Add to cart error:', error);
        setTimeout(() => {
          this.error = '';
        }, 3000);
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