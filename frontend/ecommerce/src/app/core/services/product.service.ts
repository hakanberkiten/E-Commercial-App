import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
    private api = `${environment.apiUrl}/products`;
    constructor(private http: HttpClient) { }
    getAll() { return this.http.get<Product[]>(this.api); }
    getById(id: number) { return this.http.get<Product>(`${this.api}/${id}`); }
    create(p: Product) { return this.http.post<Product>(this.api, p); }
    update(id: number, p: Product) { return this.http.put<Product>(`${this.api}/${id}`, p); }
    delete(id: number) { return this.http.delete<void>(`${this.api}/${id}`); }
}
