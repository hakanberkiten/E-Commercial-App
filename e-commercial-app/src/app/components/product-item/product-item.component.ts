import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Product, ProductsService } from '../../services/products.service';

@Component({
  selector: 'app-product-item',
  imports: [],
  templateUrl: './product-item.component.html',
  styleUrl: './product-item.component.css'
})
export class ProductItemComponent {
  @Input() product!: Product;
  @Output() productAdded = new EventEmitter<Product>();

  constructor(private productsService: ProductsService) {}

  addToCart() {
    this.productAdded.emit(this.product);
  }
}
