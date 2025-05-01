// src/app/cart-page/cart-page.component.ts
import { Component, OnInit } from '@angular/core';
import { CartService } from '../core/services/cart.service';
import { PaymentService } from '../core/services/payment.service';
import { AuthService } from '../core/services/auth.service';
import { CartItem } from '../shared/models/cartitem.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cart-page',
  standalone: false,
  templateUrl: './cart-page.component.html'
})
export class CartPageComponent implements OnInit {
  items: CartItem[] = [];
  error = '';
  savedCards: any[] = [];
  selectedPaymentMethod: string | null = null;
  isProcessing: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private cartSvc: CartService,
    private paymentSvc: PaymentService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.load();
    this.loadUserCards();
  }

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

  loadUserCards() {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    // First ensure the user has a Stripe customer account
    this.paymentSvc.createStripeCustomer(currentUser.userId).subscribe({
      next: () => {
        // Then load their cards
        this.paymentSvc.getUserCards(currentUser.userId).subscribe({
          next: (cards) => {
            this.savedCards = cards;
            if (cards.length > 0) {
              this.selectedPaymentMethod = cards[0].id;
            }
          },
          error: (error) => console.error('Error loading cards:', error)
        });
      },
      error: (error) => console.error('Error creating/getting Stripe customer:', error)
    });
  }

  checkout() {
    if (!this.selectedPaymentMethod) {
      this.errorMessage = 'Please select a payment method';
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'You must be logged in to complete this purchase';
      return;
    }

    this.isProcessing = true;

    // Create order request
    const orderItems = this.items.map(item => ({
      productId: item.product.productId,
      quantity: item.quantityInCart
    }));

    // Create order with payment
    const orderRequest = {
      userId: currentUser.userId,
      paymentMethodId: this.selectedPaymentMethod,
      items: orderItems
    };

    // Send order request
    this.cartSvc.placeOrder(orderRequest).subscribe({
      next: (response) => {
        this.isProcessing = false;
        this.successMessage = 'Order placed successfully!';

        // Clear cart after successful order
        this.items = [];

        // Navigate to order confirmation or orders page after a delay
        setTimeout(() => {
          this.router.navigate(['/profile'], { queryParams: { tab: 'orders' } });
        }, 2000);
      },
      error: (error) => {
        this.isProcessing = false;
        this.errorMessage = error.error?.message || 'Failed to place order. Please try again.';
        console.error('Checkout error:', error);
      }
    });
  }
}
