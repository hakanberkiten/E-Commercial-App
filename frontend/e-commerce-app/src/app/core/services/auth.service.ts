// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

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

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient, private router: Router) { }

  signup(data: SignupPayload): Observable<any> {
    return this.http.post('/api/auth/signup', data);
  }

  me(): Observable<User> {
    return this.http.get<User>('/api/auth/me');  // backend /api/auth/me endpoint
  }
  login(data: LoginPayload): Observable<any> {
    return this.http.post('/api/auth/login', data);
  }

  handleLoginSuccess(): void {
    this.router.navigate(['/customer']);
  }
}
