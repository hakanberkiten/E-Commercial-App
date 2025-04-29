import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../services/auth.service';
import { Router } from '@angular/router';

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

  constructor(
    private auth: AuthService,
    private fb: FormBuilder,
    private router: Router
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
  }

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadUserData();
        this.loadUserAddresses();
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

  toggleEditMode(): void {
    this.editMode = !this.editMode;
    if (!this.editMode) {
      this.loadUserData(); // İptal edildiğinde verileri geri yükle
    }
  }

  // toggleAddressMode metodunu düzelt

  toggleAddressMode(address?: any): void {
    this.editAddressMode = !this.editAddressMode;
    this.addressSuccessMessage = '';
    this.addressErrorMessage = '';

    if (this.editAddressMode && address) {
      // Mevcut adresi düzenleme
      this.selectedAddress = address;
      this.addressForm.patchValue({
        addressId: address.id,
        street: address.street,
        city: address.city,
        state: address.state,
        zipCode: address.zipCode,
        country: address.country,
        buildingName: address.buildingName, // Building Name ekledik
        isDefault: address.isDefault
      });
    } else if (this.editAddressMode) {
      // Yeni adres ekleme
      this.selectedAddress = null;
      this.addressForm.reset({
        addressId: null,
        street: '',
        city: '',
        state: '',
        zipCode: '',
        country: '',
        buildingName: '', // Building Name ekledik
        isDefault: false
      });
    } else {
      // İptal edildiğinde
      this.selectedAddress = null;
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

    // addressId -> id olarak değiştir (backend ile uyumlu olması için)
    if (addressData.addressId) {
      addressData.id = addressData.addressId;
      delete addressData.addressId;
    }

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
}