import { Injectable } from '@angular/core';
import { Interface } from 'readline';

@Injectable({
  providedIn: 'root'
})
export class ProductsService {
  categories = ['All', 'Electronics', 'Clothing', 'Home', 'Sports'];
  products = [
    { id: 1, name: 'Laptop', price: 899, category: 'Electronics', image: '/assets/laptop.jpg' },
    { id: 2, name: 'T-Shirt', price: 19, category: 'Clothing', image: '/assets/tshirt.jpg' },
    { id: 3, name: 'Coffee Maker', price: 49, category: 'Home', image: '/assets/coffeemaker.jpg' },
    { id: 4, name: 'Running Shoes', price: 79, category: 'Sports', image: '/assets/shoes.jpg' }
  ];
  filteredProducts = [...this.products];

  constructor() { }

  getProducts() {
    return this.products;
  }

  getCategories() {
    return this.categories;
  }

  filterByCategory(category: string) {
    if (category === 'All') {
      return this.filteredProducts = [...this.products];
    } else {
      return this.filteredProducts = this.products.filter(p => p.category === category);
    }
  }

  // This method should be another service method. This is just for testing.
  addToCart(product: Product) {
    alert(`${product.name} added to cart!`);
  }
}

export class Product {
  id: number;
  name: string;
  price: number;
  category: string;
  image: string;

  constructor(id: number, name: string, price: number, category: string, image: string) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
    this.image = image;
  }
}
