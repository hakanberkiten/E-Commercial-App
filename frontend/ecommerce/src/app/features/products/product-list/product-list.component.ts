import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterModule],
  template: `
  <div fxLayout="row wrap" fxLayoutGap="16px">
    <mat-card *ngFor="let p of products">
      <img mat-card-image [src]="p.productImage" alt="{{p.name}}" />
      <mat-card-title>{{p.name}}</mat-card-title>
      <mat-card-subtitle>{{p.price | currency}}</mat-card-subtitle>
      <mat-card-actions>
        <button mat-button [routerLink]="['/products',p.id]">Detay</button>
        <button mat-button color="primary" (click)="addToCart(p.id)">Sepete Ekle</button>
      </mat-card-actions>
    </mat-card>
  </div>`
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  constructor(private svc: ProductService) { }
  ngOnInit() { this.svc.getAll().subscribe(data => this.products = data) }
  addToCart(id: number) { /* CartService çağır */ }
}
