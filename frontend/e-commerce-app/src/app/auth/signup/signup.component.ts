import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
  signupForm: FormGroup;
  errorMsg = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      mobileNumber: ['', [Validators.required, this.mobileNumberValidator()]]
    });
  }

  ngOnInit(): void { }

  // Custom mobile number validator
  mobileNumberValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (!value) {
        return null; // Let required validator handle empty values
      }

      // Remove any non-digit characters
      const phoneNumber = value.replace(/\D/g, '');

      // Check if number is exactly 10 digits
      if (phoneNumber.length !== 10) {
        return { invalidLength: true };
      }

      // Check if number starts with 0
      if (phoneNumber.startsWith('0')) {
        return { startsWithZero: true };
      }

      return null;
    };
  }

  onSubmit(): void {
    this.errorMsg = '';

    if (this.signupForm.invalid) {
      return;
    }

    this.loading = true;

    console.log('Sending signup data:', this.signupForm.value);

    this.auth.signup(this.signupForm.value).subscribe({
      next: (response) => {
        console.log('Signup success:', response);
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Signup error:', err);
        this.loading = false;
        this.errorMsg = err.error?.message || err.error || 'Signup failed. Please try again.';
      }
    });
  }
}