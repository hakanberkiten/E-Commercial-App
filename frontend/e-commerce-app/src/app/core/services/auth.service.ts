import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';
import { jwtDecode } from 'jwt-decode';

interface SignupPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  mobileNumber: string;
}

interface LoginPayload {
  email: string;
  password: string;
}

export interface User {
  userId: number;
  firstName: string;
  lastName: string;
  mobileNumber: string;
  email: string;
  role: {
    roleId: number;
    roleName: string;
  };
  active: boolean; // Add this property
  stripeCustomerId?: string; // Add this line
}

interface LoginResponse {
  token: string;
  user: User;
}

interface JwtPayload {
  sub: string;
  role: number;
  userId: number;
  exp: number;
}
export interface Address {
  address_id?: string;
  userId: string;
  street: string;
  city: string;
  state: string;
  pinCode: string;
  country: string;
  isDefault: boolean;
}
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    // Sadece tarayıcı ortamında localStorage erişimi yap
    if (this.isBrowser) {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        this.currentUserSubject.next(JSON.parse(storedUser));
      }
    }
  }

  getUserAddresses(userId: string): Observable<Address[]> {
    return this.http.get<Address[]>(`/api/users/${userId}/addresses`);
  }

  // Yeni adres ekle
  addAddress(address: Address): Observable<Address> {
    return this.http.post<Address>('/api/addresses', address);
  }

  // Adres güncelle
  updateAddress(addressData: any): Observable<any> {
    // If no valid ID is provided, use POST instead of PUT
    if (!addressData.id) {
      console.log('No valid ID found, creating new address instead of updating');
      return this.addAddress(addressData);
    }
    console.log(`Updating address with ID: ${addressData.id}`);
    return this.http.put(`/api/addresses/${addressData.id}`, addressData);
  }

  changePassword(data: { userId: string, currentPassword: string, newPassword: string }): Observable<any> {
    return this.http.put('/api/users/change-password', data);
  }
  // Adres sil
  deleteAddress(addressId: string): Observable<any> {
    return this.http.delete(`/api/addresses/${addressId}`);
  }

  // Varsayılan adresi ayarla
  setDefaultAddress(addressId: string, userId: string): Observable<any> {
    return this.http.put(`/api/users/${userId}/addresses/default`, { addressId });
  }

  updateProfile(updatedProfile: { userId: string; firstName: string; lastName: string; mobileNumber: string }): Observable<User> {
    return this.http.put<User>(`/api/users/${updatedProfile.userId}`, updatedProfile).pipe(
      tap(updatedUser => {
        if (this.isBrowser) {
          localStorage.setItem('currentUser', JSON.stringify(updatedUser));
        }
        this.currentUserSubject.next(updatedUser);
      })
    );
  }

  signup(data: SignupPayload): Observable<any> {
    return this.http.post('/api/auth/signup', data);
  }


  refreshCurrentUser(): void {
    this.http.get<User>('/api/user/current').subscribe({
      next: (user) => this.currentUserSubject.next(user),
      error: (error) => this.currentUserSubject.next(null)
    });
  }
  login(data: LoginPayload): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', data)
      .pipe(
        tap(response => {
          // Sadece tarayıcı ortamında localStorage erişimi yap
          if (this.isBrowser) {
            localStorage.setItem('jwt_token', response.token);
            localStorage.setItem('currentUser', JSON.stringify(response.user));
          }
          this.currentUserSubject.next(response.user);
        })
      );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('currentUser');
    }
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }
  getCurrentUser(): User | null {
    return this.currentUser;
  }
  handleLoginSuccess(): void {
    const userRole = this.getUserRole();

    if (userRole === 'ROLE_ADMIN') {
      this.router.navigate(['/admin']);
    } else if (userRole === 'ROLE_SELLER') {
      this.router.navigate(['/seller-dashboard']);
    } else {
      this.router.navigate(['/products']);
    }
  }

  getToken(): string | null {
    if (this.isBrowser) {
      return localStorage.getItem('jwt_token');
    }
    return null;
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUserRole(): string {
    const token = this.getToken();
    if (!token) return '';

    try {
      const decoded = jwtDecode<JwtPayload>(token);
      const roleId = decoded.role;

      switch (roleId) {
        case 1: return 'ROLE_ADMIN';
        case 2: return 'ROLE_SELLER';
        case 3: return 'ROLE_CUSTOMER';
        default: return 'ROLE_USER';
      }
    } catch (e) {
      return '';
    }
  }

  hasRole(role: string): boolean {
    return this.getUserRole() === role;
  }

  // Method to handle OAuth2 login success
  handleOAuth2Success(token: string): Observable<User> {
    console.log('Processing OAuth2 success with token');

    // Store token
    if (this.isBrowser) {
      localStorage.setItem('jwt_token', token);
    }

    // Fetch user info with the token - FIXED PATH to match backend controller
    return this.http.get<User>('/api/auth/user/current').pipe(
      tap(user => {
        console.log('Fetched current user:', user);
        if (this.isBrowser) {
          localStorage.setItem('currentUser', JSON.stringify(user));
        }
        this.currentUserSubject.next(user);
      })
    );
  }
}
