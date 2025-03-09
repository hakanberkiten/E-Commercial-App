import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Product, ProductsService } from '../../services/products.service';
import { ProductItemComponent } from "../product-item/product-item.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductItemComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit{
  categories : string[] = [];
  products : Product[] = [];

  filteredProducts = [...this.products];

  IsShopNowClicked : boolean = false;

  shopNow() {
    this.IsShopNowClicked = true;
  }

  ngOnInit() {
    this.categories = this.productsService.getCategories();
    this.products = this.productsService.getProducts();
    this.filteredProducts = [...this.products];
  }

  constructor(private router: Router, private productsService: ProductsService) { }

  filterByCategory(category: string) {
    this.filteredProducts = this.productsService.filterByCategory(category);
  }

  // This is connected to the product service but just for testing.
  addToCart(product: any) {
    this.productsService.addToCart(product);
    console.log("This product added to cart: "+product.name);
  }
}
