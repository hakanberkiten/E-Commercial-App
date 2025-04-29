// src/app/core/services/product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Product } from '../../shared/models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {

  constructor(private http: HttpClient) { }

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products/all');
  }

  getByCategory(categoryId: number | null): Observable<Product[]> {
    if (categoryId === null) {
      return this.getAll();
    }
    return this.http.get<Product[]>(`/api/products?categoryId=${categoryId}`);
  }

  searchProducts(query: string): Observable<any[]> {
    return this.http.get<Product[]>(`/api/products/search?query=${query}`).pipe(
      map(products => products.map(product => ({
        id: product.productId,
        name: product.productName,
        price: product.price,
        imageUrl: product.image
      })))
    );
  }

  getProductsBySearch(query: string): Observable<Product[]> {
    return this.http.get<Product[]>(`/api/products/search?query=${query}`);
  }

  getProductById(id: number): Observable<any> {
    return this.http.get<any>(`/api/products/${id}`);
  }
}
