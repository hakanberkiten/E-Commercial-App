import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

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
  email: string;
  roleId: number;
}

interface LoginResponse {
  token: string;
  user: User;
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

  signup(data: SignupPayload): Observable<any> {
    return this.http.post('/api/auth/signup', data);
  }

  me(): Observable<User> {
    return this.http.get<User>('/api/auth/me');
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

  handleLoginSuccess(): void {
    this.router.navigate(['/customer']);
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
}