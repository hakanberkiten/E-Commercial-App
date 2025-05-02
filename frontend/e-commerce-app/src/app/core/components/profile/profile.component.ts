import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../services/auth.service';
import { Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { HttpClient } from '@angular/common/http';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';

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

  constructor(
    private auth: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private paymentService: PaymentService,
    private http: HttpClient
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
                  color: '#32325d',
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
    if (!this.currentUser || this.isAdmin()) return; // Skip for admin

    this.loading = true;

    // First ensure the user has a Stripe customer account
    this.paymentService.createStripeCustomer(this.currentUser.userId).subscribe({
      next: (customerId) => {
        // Then load their cards
        this.paymentService.getUserCards(this.currentUser?.userId || 0).subscribe({
          next: (cards) => {
            this.savedCards = cards;
            this.loading = false;
          },
          error: (error) => {
            console.error('Error loading cards:', error);
            this.loading = false;
          }
        });
      },
      error: (error) => {
        console.error('Error creating/getting Stripe customer:', error);
        this.loading = false;
      }
    });
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
    if (this.currentUser) {
      this.loadingOrders = true;

      this.http.get<any>(`/api/orders/user/${this.currentUser.userId}`)
        .subscribe({
          next: (response) => {
            console.log('Orders API response:', response);

            // Handle both array and object responses
            if (Array.isArray(response)) {
              this.userOrders = response;
            } else if (response && typeof response === 'object') {
              // Check if response is wrapped in a data property
              if (Array.isArray(response.data)) {
                this.userOrders = response.data;
              } else {
                // Convert object to array if needed
                this.userOrders = [response];
              }
            } else {
              this.userOrders = [];
            }

            console.log('Processed orders:', this.userOrders);
            this.loadingOrders = false;
          },
          error: (error) => {
            console.error('Error loading orders', error);
            this.errorMessage = 'Failed to load your orders. Please try again.';
            this.loadingOrders = false;
          }
        });
    }
  }

  toggleOrderDetails(orderId: number) {
    if (this.expandedOrderId === orderId) {
      this.expandedOrderId = null;
    } else {
      this.expandedOrderId = orderId;
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

      // Double-check that ID was set correctly
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
      next: (response) => {
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

    // Check if addressId exists and is not null/undefined before converting
    if (addressData.addressId !== undefined && addressData.addressId !== null) {
      addressData.id = addressData.addressId;
      delete addressData.addressId;
    }

    // Log the addressData to debug
    console.log('Address data being submitted:', addressData);

    // Check if it's an update (has valid ID) or new address
    const request = addressData.id
      ? this.auth.updateAddress(addressData)
      : this.auth.addAddress(addressData);

    request.subscribe({
      next: (response) => {
        this.addressLoading = false;
        this.addressSuccessMessage = addressData.id
          ? 'Address updated successfully!'
          : 'Address added successfully!';
        this.editAddressMode = false;
        this.loadUserAddresses(); // Adresleri yeniden yükle
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
        this.loadUserAddresses(); // Adresleri yeniden yükle
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
        this.loadUserAddresses(); // Adresleri yeniden yükle
      },
      error: (err) => {
        this.addressErrorMessage = 'Failed to update default address';
        console.error('Error updating default address:', err);
      }
    });
  }

  // Change Password modunu aç/kapat
  toggleChangePasswordMode(): void {
    this.changePasswordMode = !this.changePasswordMode;
    if (this.changePasswordMode) {
      this.editMode = false; // Profil düzenleme modunu kapat
      this.editAddressMode = false; // Adres düzenleme modunu kapat
      this.passwordForm.reset();
    }
    this.passwordSuccessMessage = '';
    this.passwordErrorMessage = '';
  }

  // Şifre değiştirme işlemi
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

        // 3 saniye sonra şifre değiştirme modunu kapat
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

  // Add a new card
  async addNewCard(): Promise<void> {
    console.log('Add card button clicked'); // Debug log
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
      // Create payment method with card element
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
        // Send only the payment method ID to your backend
        this.paymentService.addCardToken(
          this.currentUser.userId,
          result.paymentMethod.id
        ).subscribe({
          next: (response) => {
            console.log('Card added successfully:', response);
            this.cardSubmitting = false;
            this.cardSuccess = 'Card added successfully!';
            // Reset the card element for future use
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

  // Set card as default
  setDefaultCard(cardId: string): void {
    // For this implementation, we'll just designate this as the default in the UI
    // You could extend the backend to support this if needed
    alert('Default card functionality would be implemented here');
  }

  // Add this method to the ProfileComponent
  isAdmin(): boolean {
    return this.currentUser?.role?.roleName === 'ADMIN' ||
      this.currentUser?.role?.roleName === 'ROLE_ADMIN';
  }

  // Update this method to allow admins to access both profile and addresses tabs
  setActiveTab(tab: string): void {
    this.activeTab = tab;

    // Load the appropriate data based on tab
    if (tab === 'myorders') {
      this.loadUserOrders();
    } else if (tab === 'payment') {
      this.loadUserCards();
    } else if (tab === 'orders') {
      this.loadPaymentHistory();
    }
  }

  // Add this as a utility method
  hasOrders(): boolean {
    const hasOrdersValue = Array.isArray(this.userOrders) && this.userOrders.length > 0;
    console.log('Has orders check:', hasOrdersValue, this.userOrders);
    return hasOrdersValue;
  }
}
