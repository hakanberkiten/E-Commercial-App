import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignUpComponent {
  navigateToLogin() {
    this.router.navigate(['/login']);
  }
  name: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';

  constructor(private router: Router) { }

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    console.log('Name:', this.name);
    console.log('Email:', this.email);
    console.log('Password:', this.password);

    alert('Registration successful!');
    this.router.navigate(['/login']);
  }
}
