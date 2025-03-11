import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignUpComponent {
  signupForm: FormGroup;

  constructor(private router: Router) {
    this.signupForm = new FormGroup({
      name: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmPassword: new FormControl('', Validators.required)
    });
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
  }

  onSubmit() {

    const emailControl = this.signupForm.get('email');
    if (emailControl && emailControl.invalid) {
      alert("Please enter a valid email address!");
      return;
    }


    if (this.signupForm.valid) {
      const formValues = this.signupForm.value;


      if (this.passwordsDoNotMatch) {

        return;
      }

      console.log('Name:', formValues.name);
      console.log('Email:', formValues.email);
      console.log('Password:', formValues.password);

      alert('Registration successful!');
      this.router.navigate(['/login']);
    }
  }
  get password() { return this.signupForm.get('password'); }
  get confirmPassword() { return this.signupForm.get('confirmPassword'); }


  get passwordsDoNotMatch(): boolean {
    const password = this.password?.value;
    const confirmPassword = this.confirmPassword?.value;
    return (password && confirmPassword && password !== confirmPassword) ? true : false;
  }
}