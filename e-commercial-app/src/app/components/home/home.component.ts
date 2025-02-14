import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  categories = ['All', 'Electronics', 'Clothing', 'Home', 'Sports'];
  products = [
    { id: 1, name: 'Laptop', price: 899, category: 'Electronics', image: '/assets/laptop.jpg' },
    { id: 2, name: 'T-Shirt', price: 19, category: 'Clothing', image: '/assets/tshirt.jpg' },
    { id: 3, name: 'Coffee Maker', price: 49, category: 'Home', image: '/assets/coffeemaker.jpg' },
    { id: 4, name: 'Running Shoes', price: 79, category: 'Sports', image: '/assets/shoes.jpg' }
  ];

  filteredProducts = [...this.products];

  constructor(private router: Router) { }

  filterByCategory(category: string) {
    if (category === 'All') {
      this.filteredProducts = [...this.products];
    } else {
      this.filteredProducts = this.products.filter(p => p.category === category);
    }
  }

  addToCart(product: any) {
    alert(`${product.name} added to cart!`);
  }
}
