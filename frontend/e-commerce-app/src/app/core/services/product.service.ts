// src/app/core/services/product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../../shared/models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {

  constructor(private http: HttpClient) { }

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products/all');
  }
  getByCategory(categoryId: number): Observable<Product[]> {
    // Implementation for fetching products by category
    // Replace the following line with actual API call logic
    return new Observable<Product[]>(observer => {
      observer.next([]); // Example: return an empty array
      observer.complete();
    });
  }

}
