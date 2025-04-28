// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMsg = '';

  constructor(private fb: FormBuilder, private auth: AuthService) { }

  ngOnInit() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.errorMsg = '';
    if (!this.loginForm.valid) { return; }
    this.auth.login(this.loginForm.value)
      .subscribe({
        next: () => this.auth.handleLoginSuccess(),
        error: () => this.errorMsg = 'Invalid email or password'
      });
  }
}
