import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;

  constructor(private auth: AuthService) { }

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.auth.logout();
  }

  isCustomer(): boolean {
    return this.auth.getUserRole() === 'ROLE_CUSTOMER';
  }

  isSeller(): boolean {
    return this.auth.getUserRole() === 'ROLE_SELLER';
  }

  isAdmin(): boolean {
    return this.auth.getUserRole() === 'ROLE_ADMIN';
  }

  toggleDropdown(event: Event) {
    event.preventDefault();
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) {
      dropdown.classList.toggle('show');
    }
  }
}