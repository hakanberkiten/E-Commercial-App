// src/app/auth/signup/signup.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']

})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  errorMsg = '';

  constructor(private fb: FormBuilder, private auth: AuthService) { }

  ngOnInit() {
    this.signupForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      mobileNumber: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.errorMsg = '';
    if (!this.signupForm.valid) { return; }
    this.auth.signup(this.signupForm.value)
      .subscribe({
        next: () => this.auth.handleLoginSuccess(),  // go to products
        error: err => this.errorMsg = err.error || 'Signup failed'
      });
  }
}
