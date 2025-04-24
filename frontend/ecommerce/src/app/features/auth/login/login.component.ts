import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <mat-form-field appearance="outline">
      <mat-label>Username</mat-label>
      <input matInput formControlName="username" />
    </mat-form-field>
    <mat-form-field appearance="outline">
      <mat-label>Password</mat-label>
      <input matInput type="password" formControlName="password" />
    </mat-form-field>
    <button mat-raised-button color="primary" type="submit">Login</button>
  </form>`
})
export class LoginComponent {
  form: any;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    const { username, password } = this.form.value;
    this.auth.login(username!, password!)
      .subscribe({
        next: () => { this.toastr.success('Logged in'); this.router.navigate(['/products']); },
        error: () => this.toastr.error('Login failed')
      });
  }
}
