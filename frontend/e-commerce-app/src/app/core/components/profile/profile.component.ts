// src/app/profile/profile.component.ts

import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../services/auth.service';
import { Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { HttpClient } from '@angular/common/http';
import { OrderService } from '../../services/order.service';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';
import { isPlatformBrowser } from '@angular/common';
import { map } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  profileForm: FormGroup;
  addressForm: FormGroup;
  loading = false;
  addressLoading = false;
  successMessage = '';
  errorMessage = '';
  addressSuccessMessage = '';
  addressErrorMessage = '';
  editMode = false;
  editAddressMode = false;
  addresses: any[] = [];
  selectedAddress: any = null;

  // Şifre değiştirme için flag ve form
  changePasswordMode = false;
  passwordForm: FormGroup;
  passwordLoading = false;
  passwordSuccessMessage = '';
  passwordErrorMessage = '';

  // Payment-related properties
  savedCards: any[] = [];
  cardSubmitting: boolean = false;
  cardError: string = '';
  cardSuccess: string = '';
  cardForm: FormGroup;
  months: string[] = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];
  years: string[] = [];

  // Payment history
  paymentHistory: any[] = [];

  // Stripe properties
  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  private card: StripeCardElement | null = null;

  // Tab navigation
  activeTab: string = 'profile'; // Default active tab

  // Orders-related properties
  userOrders: any[] = [];
  loadingOrders: boolean = false;
  expandedOrderId: number | null = null;
  loadingOrderDetails: boolean = false;

  // Return-related properties
  isProcessingReturn = false;
  showConfirmDialog = false;
  currentReturnOrderId: number | null = null;
  orderToReturn: any = null;

  constructor(
    private auth: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private http: HttpClient,
    private orderService: OrderService,
    private paymentService: PaymentService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [{ value: '', disabled: true }],
      mobileNumber: ['', Validators.required]
    });

    this.addressForm = this.fb.group({
      addressId: [null],
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      zipCode: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
      country: ['', Validators.required],
      buildingName: ['', Validators.required],
      isDefault: [false]
    });

    // Şifre değiştirme formu
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });

    // Create years array for the next 10 years
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 10; i++) {
      this.years.push((currentYear + i).toString().substr(-2));
    }

    // Initialize the card form
    this.cardForm = this.fb.group({
      cardNumber: ['', [Validators.required, Validators.pattern(/^\d{16}$/)]],
      expirationMonth: ['', Validators.required],
      expirationYear: ['', Validators.required],
      cvc: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]]
    });
  }

  // Şifre eşleşme kontrolü için validator
  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (newPassword !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({ mismatch: true });
      return { mismatch: true };
    } else {
      return null;
    }
  }

  async ngOnInit(): Promise<void> {
    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadUserData();
        this.loadUserAddresses();

        // Only load payment/order related data for non-admins
        if (!this.isAdmin()) {
          this.loadUserCards();
          this.loadPaymentHistory();

          // Add this line to load orders on init
          this.loadUserOrders();

          // Initialize Stripe only for non-admins
          this.initializeStripe();
        } else {
          // For admin users, ensure we stay on the profile tab
          this.activeTab = 'profile';
        }

        // Check for tab parameter in URL
        const urlParams = new URLSearchParams(window.location.search);
        const tabParam = urlParams.get('tab');
        if (tabParam) {
          this.setActiveTab(tabParam);
        }
      } else {
        this.router.navigate(['/login']);
      }
    });
  }

  async initializeStripe() {
    // Replace with your Stripe publishable key
    this.stripe = await loadStripe('pk_test_51RJwDuCLsCTArTvazVDjPgLtI1RLPbXYiKBdPVvheryzEOnor13kZpAYWWlAXwBAg8oPRevSWMlaaQmllo6nO2Ue00GEnd6YMg');

    if (this.stripe) {
      this.elements = this.stripe.elements();

      // Use a longer timeout to ensure DOM is fully loaded
      setTimeout(() => {
        const cardElement = document.getElementById('card-element');
        console.log('Card element found:', !!cardElement);

        if (cardElement && this.elements) {
          try {
            // Create card element with custom options
            this.card = this.elements.create('card', {
              hidePostalCode: true, // Disables ZIP/postal code field
              style: {
                base: {
                  fontSize: '16px',
                  color: '#8a6fff',
                }
              }
            });

            this.card.mount('#card-element');

            // Handle validation errors
            this.card.on('change', (event) => {
              const displayError = document.getElementById('card-errors');
              if (displayError) {
                displayError.textContent = event.error ? event.error.message : '';
              }
            });
            console.log('Card element mounted successfully');
          } catch (error) {
            console.error('Error creating/mounting card element:', error);
          }
        } else {
          console.error('Card element not found in DOM');
        }
      }, 500); // Longer timeout
    }
  }

  loadUserData(): void {
    if (!this.currentUser) return;

    // Kullanıcı verisini form'a yükle
    this.profileForm.patchValue({
      firstName: this.currentUser.firstName,
      lastName: this.currentUser.lastName,
      email: this.currentUser.email,
      mobileNumber: this.currentUser.mobileNumber || ''
    });
  }

  loadUserAddresses(): void {
    if (!this.currentUser?.userId) return;

    this.auth.getUserAddresses(this.currentUser.userId.toString()).subscribe({
      next: (addresses) => {
        this.addresses = addresses;
      },
      error: (err) => {
        this.addressErrorMessage = 'Failed to load addresses';
        console.error('Error loading addresses:', err);
      }
    });
  }

  loadUserCards(): void {
    this.loading = true;
    const userId = this.auth.getCurrentUser()?.userId;

    if (userId) {
      this.paymentService.getUserCards(userId).subscribe({
        next: (cards) => {
          this.savedCards = cards;
          console.log('Cards loaded:', this.savedCards);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading cards:', error);
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  loadPaymentHistory(): void {
    const currentUser = this.auth.getCurrentUser();
    if (!currentUser || this.isAdmin()) return; // Skip for admin

    this.http.get<any[]>(`/api/payments/user/${currentUser.userId}`).subscribe({
      next: (payments) => {
        // Add a date field if missing
        this.paymentHistory = payments.map(payment => {
          // Set a default date if none exists
          if (!payment.paymentDate && !payment.createdAt && !payment.payment_date) {
            payment.paymentDate = new Date();
          }
          return payment;
        });
        console.log('Payment history loaded:', this.paymentHistory);
      },
      error: (error) => {
        console.error('Error loading payment history:', error);
      }
    });
  }

  loadUserOrders() {
    this.loadingOrders = true;
    const currentUser = this.auth.getCurrentUser();
    if (!currentUser) {
      this.loadingOrders = false;
      return;
    }

    // Add timestamp to prevent caching
    const timestamp = new Date().getTime();
    this.orderService.getOrdersByUserId(currentUser.userId).subscribe({
      next: (orders) => {
        this.userOrders = orders;
        this.loadingOrders = false;
      },
      error: (error) => {
        console.error('Error loading orders', error);
        this.errorMessage = 'Failed to load your orders. Please try again.';
        this.loadingOrders = false;
      }
    });
  }

  toggleOrderDetails(orderId: number): void {
    if (this.expandedOrderId === orderId) {
      // If already expanded, just collapse
      this.expandedOrderId = null;
    } else {
      // If expanding, fetch fresh data for this specific order
      this.loadingOrderDetails = true;

      // Add timestamp to prevent caching
      this.orderService.getOrderById(orderId).subscribe({
        next: (orderDetails) => {
          console.log('Fetched order details:', orderDetails);

          // Replace the entire order object with the fresh data
          const index = this.userOrders.findIndex(order => order.orderId === orderId);
          if (index !== -1) {
            // Ensure we fully update the order object with the fresh data
            this.userOrders[index] = orderDetails;
          }

          this.expandedOrderId = orderId;
          this.loadingOrderDetails = false;
        },
        error: (error) => {
          console.error('Error loading order details', error);
          this.errorMessage = 'Failed to load order details. Please try again.';
          this.loadingOrderDetails = false;
        }
      });
    }
  }

  toggleEditMode(): void {
    this.editMode = !this.editMode;
    if (!this.editMode) {
      this.loadUserData(); // İptal edildiğinde verileri geri yükle
    }
  }

  toggleAddressMode(address?: any): void {
    this.editAddressMode = !this.editAddressMode;
    this.addressSuccessMessage = '';
    this.addressErrorMessage = '';

    if (this.editAddressMode && address) {
      // Mevcut adresi düzenleme - ensure ID is properly set
      this.selectedAddress = address;
      console.log('Editing address:', address); // Add debug log
      this.addressForm.patchValue({
        addressId: address.id, // Make sure this is not undefined
        street: address.street,
        city: address.city,
        state: address.state,
        zipCode: address.zipCode,
        country: address.country,
        buildingName: address.buildingName,
        isDefault: address.isDefault
      });
      console.log('Form value after patch:', this.addressForm.value);
    } else if (this.editAddressMode) {
      // Reset form for new address
      this.selectedAddress = null;
      this.addressForm.reset({
        addressId: null,
        street: '',
        city: '',
        state: '',
        zipCode: '',
        country: '',
        buildingName: '',
        isDefault: false
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updatedProfile = {
      userId: this.currentUser?.userId?.toString() || '',
      firstName: this.profileForm.value.firstName,
      lastName: this.profileForm.value.lastName,
      mobileNumber: this.profileForm.value.mobileNumber
    };

    this.auth.updateProfile(updatedProfile).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Profile updated successfully!';
        this.editMode = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to update profile. Please try again.';
      }
    });
  }

  onAddressSubmit(): void {
    if (this.addressForm.invalid) return;

    this.addressLoading = true;
    this.addressErrorMessage = '';
    this.addressSuccessMessage = '';

    const addressData = {
      ...this.addressForm.value,
      userId: this.currentUser?.userId.toString(),
    };

    if (addressData.addressId !== undefined && addressData.addressId !== null) {
      addressData.id = addressData.addressId;
      delete addressData.addressId;
    }

    console.log('Address data being submitted:', addressData);

    const request = addressData.id
      ? this.auth.updateAddress(addressData)
      : this.auth.addAddress(addressData);

    request.subscribe({
      next: () => {
        this.addressLoading = false;
        this.addressSuccessMessage = addressData.id
          ? 'Address updated successfully!'
          : 'Address added successfully!';
        this.editAddressMode = false;
        this.loadUserAddresses();
      },
      error: (err) => {
        this.addressLoading = false;
        this.addressErrorMessage = err.error?.message || 'Failed to save address. Please try again.';
        console.error('Address save error:', err);
      }
    });
  }

  deleteAddress(addressId: string): void {
    if (!confirm('Are you sure you want to delete this address?')) return;

    this.auth.deleteAddress(addressId).subscribe({
      next: () => {
        this.addressSuccessMessage = 'Address deleted successfully!';
        this.loadUserAddresses();
      },
      error: (err) => {
        this.addressErrorMessage = 'Failed to delete address';
        console.error('Error deleting address:', err);
      }
    });
  }

  setDefaultAddress(addressId: string): void {
    this.auth.setDefaultAddress(addressId, this.currentUser?.userId?.toString() || '').subscribe({
      next: () => {
        this.addressSuccessMessage = 'Default address updated!';
        this.loadUserAddresses();
      },
      error: (err) => {
        this.addressErrorMessage = 'Failed to update default address';
        console.error('Error updating default address:', err);
      }
    });
  }

  toggleChangePasswordMode(): void {
    this.changePasswordMode = !this.changePasswordMode;
    if (this.changePasswordMode) {
      this.editMode = false;
      this.editAddressMode = false;
      this.passwordForm.reset();
    }
    this.passwordSuccessMessage = '';
    this.passwordErrorMessage = '';
  }

  onPasswordSubmit(): void {
    if (this.passwordForm.invalid) return;

    this.passwordLoading = true;
    this.passwordErrorMessage = '';
    this.passwordSuccessMessage = '';

    if (!this.currentUser?.userId) {
      this.passwordLoading = false;
      this.passwordErrorMessage = 'User ID is missing. Please try again.';
      return;
    }

    const passwordData = {
      userId: this.currentUser.userId.toString(),
      currentPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword
    };

    this.auth.changePassword(passwordData).subscribe({
      next: () => {
        this.passwordLoading = false;
        this.passwordSuccessMessage = 'Password changed successfully!';
        this.passwordForm.reset();

        setTimeout(() => {
          this.changePasswordMode = false;
          this.passwordSuccessMessage = '';
        }, 3000);
      },
      error: (err) => {
        this.passwordLoading = false;
        this.passwordErrorMessage = err.error?.message || 'Failed to change password. Please try again.';
      }
    });
  }

  async addNewCard(): Promise<void> {
    console.log('Add card button clicked');
    console.log('Stripe available:', !!this.stripe);
    console.log('Card element available:', !!this.card);
    console.log('Current user available:', !!this.currentUser);

    if (!this.stripe || !this.card || !this.currentUser) {
      console.error('Required objects not available:',
        { stripe: !!this.stripe, card: !!this.card, user: !!this.currentUser });
      this.cardError = 'Payment system not fully initialized. Please refresh and try again.';
      return;
    }

    this.cardSubmitting = true;
    this.cardError = '';
    this.cardSuccess = '';

    try {
      console.log('Creating payment method...');
      const result = await this.stripe.createPaymentMethod({
        type: 'card',
        card: this.card
      });

      console.log('Payment method result:', result);

      if (result.error) {
        this.cardError = result.error.message || 'An error occurred';
        this.cardSubmitting = false;
        return;
      }

      if (result.paymentMethod) {
        console.log('Sending payment method to backend...');
        this.paymentService.addCardToken(
          this.currentUser.userId,
          result.paymentMethod.id
        ).subscribe({
          next: () => {
            console.log('Card added successfully');
            this.cardSubmitting = false;
            this.cardSuccess = 'Card added successfully!';
            if (this.card) {
              this.card.clear();
            }
            this.loadUserCards();
            setTimeout(() => { this.cardSuccess = ''; }, 3000);
          },
          error: (error) => {
            console.error('Error from backend:', error);
            this.cardSubmitting = false;
            this.cardError = error.error?.message || 'Failed to add card';
          }
        });
      }
    } catch (error) {
      console.error('Unexpected error during card addition:', error);
      this.cardSubmitting = false;
      this.cardError = 'An unexpected error occurred';
    }
  }

  setDefaultCard(cardId: string): void {
    alert('Default card functionality would be implemented here');
  }

  isAdmin(): boolean {
    return this.currentUser?.role?.roleName === 'ADMIN' ||
      this.currentUser?.role?.roleName === 'ROLE_ADMIN';
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'myorders') {
      this.loadUserOrders();
    } else if (tab === 'payment') {
      this.loadUserCards();
    } else if (tab === 'orders') {
      this.loadPaymentHistory();
    }
  }

  hasOrders(): boolean {
    const hasOrdersValue = Array.isArray(this.userOrders) && this.userOrders.length > 0;
    console.log('Has orders check:', hasOrdersValue, this.userOrders);
    return hasOrdersValue;
  }

  canBeReturned(order: any): boolean {
    return order.orderStatus !== 'CANCELLED' &&
      ['PENDING', 'PROCESSING', 'SHIPPED', 'PARTIALLY_SHIPPED'].includes(order.orderStatus);
  }

  showReturnConfirmation(order: any): void {
    this.orderToReturn = order;
    this.showConfirmDialog = true;
  }

  hideReturnConfirmation(): void {
    this.showConfirmDialog = false;
    this.orderToReturn = null;
  }

  processOrderReturn(): void {
    if (!this.orderToReturn) return;

    this.isProcessingReturn = true;

    this.orderService.returnOrder(this.orderToReturn.orderId).subscribe({
      next: () => {
        const index = this.userOrders.findIndex(
          order => order.orderId === this.orderToReturn.orderId
        );

        if (index !== -1) {
          this.userOrders[index].orderStatus = 'CANCELLED';
        }

        this.hideReturnConfirmation();

        this.successMessage = 'Your order has been returned successfully and a refund has been processed to your original payment method.';

        if (isPlatformBrowser(this.platformId)) {
          window.scrollTo(0, 0);
        }
      },
      error: (error) => {
        console.error('Error returning order:', error);

        this.hideReturnConfirmation();

        this.errorMessage = error.error?.message || 'Failed to process your return request. Please contact customer support.';

        if (isPlatformBrowser(this.platformId)) {
          window.scrollTo(0, 0);
        }
      }
    });
  }
}
