// src/app/core/services/product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
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

  // Filtreleme metodu
  getFilteredProducts(categoryId: number | null, minPrice: number, maxPrice: number): Observable<Product[]> {
    let params = new HttpParams();

    if (categoryId !== null && categoryId > 0) {
      params = params.set('categoryId', categoryId.toString());
    }

    params = params.set('minPrice', minPrice.toString());
    params = params.set('maxPrice', maxPrice.toString());

    return this.http.get<Product[]>('/api/products/filter', { params });
  }


  getProductsBySellerId(sellerId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/products/seller/${sellerId}`);
  }

  createProduct(product: any): Observable<any> {
    // Change this to use /save endpoint
    return this.http.post<any>('/api/products/save', product);
  }

  updateProduct(product: any): Observable<any> {
    // Change this to use productId instead of id
    return this.http.put<any>(`/api/products/${product.productId}`, product);
  }

  deleteProduct(productId: number): Observable<any> {
    // This should match the backend endpoint
    return this.http.delete<any>(`/api/products/${productId}`);
  }
}
