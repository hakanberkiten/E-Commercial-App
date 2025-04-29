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
  getByCategory(categoryId: number | null): Observable<Product[]> {
    if (categoryId === null) {
      return this.getAll();
    }
    return this.http.get<Product[]>(`/api/products?categoryId=${categoryId}`);
  }

}
