import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../services/auth.service';
import { Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { HttpClient } from '@angular/common/http';

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

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadUserData();
        this.loadUserAddresses();
        this.loadUserCards();
        this.loadPaymentHistory();
      } else {
        this.router.navigate(['/login']);
      }
    });
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
    if (!this.currentUser) return;

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
    if (!currentUser) return;

    this.http.get<any[]>(`/api/payments/user/${currentUser.userId}`).subscribe({
      next: (payments) => {
        this.paymentHistory = payments;
      },
      error: (error) => {
        console.error('Error loading payment history:', error);
      }
    });
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
  addNewCard(): void {
    if (this.cardForm.invalid) return;

    if (!this.currentUser) return;

    this.cardSubmitting = true;
    this.cardError = '';
    this.cardSuccess = '';

    const cardData = {
      cardNumber: this.cardForm.value.cardNumber,
      expirationMonth: this.cardForm.value.expirationMonth,
      expirationYear: this.cardForm.value.expirationYear,
      cvc: this.cardForm.value.cvc
    };

    this.paymentService.addCard(this.currentUser.userId, cardData).subscribe({
      next: (result) => {
        this.cardSubmitting = false;
        this.cardSuccess = 'Card added successfully!';
        this.cardForm.reset();

        // Reload cards
        this.loadUserCards();

        // Clear success message after 3 seconds
        setTimeout(() => {
          this.cardSuccess = '';
        }, 3000);
      },
      error: (error) => {
        this.cardSubmitting = false;
        this.cardError = error.error?.message || 'Failed to add card. Please try again.';
        console.error('Error adding card:', error);
      }
    });
  }

  // Set card as default
  setDefaultCard(cardId: string): void {
    // For this implementation, we'll just designate this as the default in the UI
    // You could extend the backend to support this if needed
    alert('Default card functionality would be implemented here');
  }
}
