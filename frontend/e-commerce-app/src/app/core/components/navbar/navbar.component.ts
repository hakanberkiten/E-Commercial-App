import { Component, OnInit, HostListener } from '@angular/core';
import { AuthService, User } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { Router } from '@angular/router';
import { debounceTime, Subject } from 'rxjs';

interface SearchResult {
  id: number;
  name: string;
  price: number;
  imageUrl?: string;
}

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  searchQuery: string = '';
  searchResults: SearchResult[] = [];
  showSearchResults: boolean = false;
  private searchSubject = new Subject<string>();
  searchLoading: boolean = false;

  constructor(
    private auth: AuthService,
    private productService: ProductService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    // Arama için debounce zamanlaması
    this.searchSubject.pipe(
      debounceTime(300) // 300ms bekle
    ).subscribe(query => {
      this.performSearch(query);
    });
  }

  // Arama input'u değiştiğinde
  onSearchChange(event: any): void {
    const query = event.target.value;
    this.searchSubject.next(query);
  }

  // Asıl arama fonksiyonu
  private performSearch(query: string): void {
    if (!query || query.trim() === '') {
      this.searchResults = [];
      return;
    }

    this.searchLoading = true;
    this.productService.searchProducts(query).subscribe({
      next: (results) => {
        this.searchResults = results;
        this.searchLoading = false;
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.searchLoading = false;
      }
    });
  }

  // Arama butonuna tıklandığında
  submitSearch(): void {
    if (this.searchQuery && this.searchQuery.trim() !== '') {
      this.router.navigate(['/products'], {
        queryParams: { search: this.searchQuery }
      });
      this.showSearchResults = false;
    }
  }

  // Ürün detaylarına git
  goToProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
    this.showSearchResults = false;
    this.searchQuery = '';
  }

  // Arama input odaklandığında
  onSearchFocus(): void {
    this.showSearchResults = true;
  }

  // Arama input'tan çıkıldığında
  onSearchBlur(): void {
    // Kullanıcı dropdown'a tıklarken sonuçların kapanmaması için biraz geciktirme ekleyin
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  // Diğer mevcut metodlar
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