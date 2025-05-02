// src/app/cart-page/cart-page.component.ts
import { Component, OnInit } from '@angular/core';
import { CartService } from '../core/services/cart.service';
import { PaymentService } from '../core/services/payment.service';
import { AuthService } from '../core/services/auth.service';
import { CartItem } from '../shared/models/cartitem.model';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-cart-page',
  standalone: false,
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.css']
})
export class CartPageComponent implements OnInit {
  items: CartItem[] = [];
  total: number = 0;
  error: string = '';
  savedCards: any[] = [];
  selectedPaymentMethod: string = '';
  isProcessing: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  addresses: any[] = []; // New property to hold user addresses
  isSettingDefault: boolean = false; // Add to the existing properties

  constructor(
    private cartService: CartService,
    private paymentSvc: PaymentService,
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCartItems();
    this.loadPaymentMethods();
    this.loadUserAddresses(); // Add this method call
  }

  loadCartItems(): void {
    this.cartService.list()
      .subscribe(i => this.items = i, e => this.error = e);
  }

  // Update loadPaymentMethods method to identify default card
  loadPaymentMethods(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    // First ensure the user has a Stripe customer account
    this.paymentSvc.createStripeCustomer(currentUser.userId).subscribe({
      next: () => {
        // Then load their cards
        this.paymentSvc.getUserCards(currentUser.userId).subscribe({
          next: (cards) => {
            this.savedCards = cards;
            const defaultCard = cards.find(card => card.isDefault === true);

            // If there's a default card, select it
            if (defaultCard) {
              this.selectedPaymentMethod = defaultCard.id;
            } else if (cards.length > 0) {
              this.selectedPaymentMethod = cards[0].id;
            }
          },
          error: (error) => console.error('Error loading cards:', error)
        });
      },
      error: (error) => console.error('Error creating/getting Stripe customer:', error)
    });
  }

  // Add this new method to load user addresses
  loadUserAddresses(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.http.get<any[]>(`/api/users/${currentUser.userId}/addresses`).subscribe({
        next: (addresses) => {
          this.addresses = addresses;
        },
        error: (err) => {
          console.error('Error loading addresses:', err);
          this.addresses = [];
        }
      });
    }
  }

  update(item: CartItem, newQuantity: string): void {
    const q = +newQuantity;
    if (q > 0)
      this.cartService.update(item.cartItemId, q)
        .subscribe(() => this.loadCartItems(), e => this.error = e);
  }

  remove(item: CartItem): void {
    this.cartService.remove(item.cartItemId)
      .subscribe(() => this.loadCartItems(), e => this.error = e);
  }

  checkUserPermission(): boolean {
    const userRole = this.authService.getUserRole();
    if (userRole !== 'ROLE_CUSTOMER' && userRole !== 'ROLE_SELLER') {
      this.errorMessage = 'Your account doesn\'t have permission to make purchases';
      return false;
    }
    return true;
  }

  checkout(): void {
    if (!this.selectedPaymentMethod) {
      this.errorMessage = 'Please select a payment method';
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'You must be logged in to complete this purchase';
      return;
    }

    if (!this.checkUserPermission()) {
      return;
    }

    if (this.addresses.length === 0) {
      this.errorMessage = "Please add a shipping address before checkout";
      return;
    }

    // Add this section to debug
    const token = localStorage.getItem('jwt_token');
    console.log('Current token:', token ? 'Present' : 'Missing');
    console.log('User role:', this.authService.getUserRole());
    console.log('User details:', currentUser);

    if (!token) {
      // If token is missing, redirect to login
      this.errorMessage = 'Your session has expired. Please login again.';
      setTimeout(() => {
        this.authService.logout();
        this.router.navigate(['/login'], { queryParams: { returnUrl: '/cart' } });
      }, 2000);
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

    // Add explicit headers to the request
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Use direct HTTP call instead of the service method for testing
    this.http.post<any>(
      '/api/orders/place',
      orderRequest,
      { headers }
    ).subscribe({
      next: (response) => {
        this.isProcessing = false;
        this.successMessage = 'Order placed successfully!';

        // Clear cart items one by one from the server
        const clearCartPromises = this.items.map(item =>
          this.cartService.remove(item.cartItemId).toPromise()
        );

        Promise.all(clearCartPromises)
          .then(() => {
            // Clear the local items array
            this.items = [];

            // Navigate to order confirmation or orders page after a delay
            setTimeout(() => {
              this.router.navigate(['/profile'], { queryParams: { tab: 'orders' } });
            }, 2000);
          })
          .catch(error => {
            console.error('Error clearing cart:', error);
          });
      },
      error: (error) => {
        this.isProcessing = false;
        this.errorMessage = error.error?.message || 'Failed to place order. Please try again.';
        console.error('Checkout error:', error);

        // Add more detailed logging for debugging
        console.error('Status:', error.status);
        console.error('Headers:', error.headers);
        console.error('Error details:', error.error);

        // If token issue, suggest re-login
        if (error.status === 401) {
          this.errorMessage = 'Your session may have expired. Please try logging in again.';
        }
      }
    });
  }

  // Add method to set default payment method
  setDefaultPaymentMethod(paymentMethodId: string): void {
    if (this.isSettingDefault) return;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.isSettingDefault = true;

    this.paymentSvc.setDefaultPaymentMethod(currentUser.userId, paymentMethodId).subscribe({
      next: () => {
        // Update local data to reflect the change
        this.savedCards = this.savedCards.map(card => ({
          ...card,
          isDefault: card.id === paymentMethodId
        }));

        // Auto-select the default card
        this.selectedPaymentMethod = paymentMethodId;

        this.isSettingDefault = false;
        this.successMessage = 'Default payment method updated';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Error setting default payment method:', error);
        this.isSettingDefault = false;
        this.errorMessage = 'Failed to set default payment method. Please try again.';
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }
}
