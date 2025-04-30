// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMsg = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        const role = response.user.role.roleName;

        if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else if (role === 'SELLER' || role === 'ROLE_SELLER') {
          this.router.navigate(['/seller-dashboard']);
        } else {
          this.router.navigate(['/products']);
        }
      },
      error: (error) => {
        this.errorMsg = error.error.message || 'Invalid credentials';
      }
    });
  }
}
