import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  constructor(private http: HttpClient) { }

  getReviewsByProduct(productId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/reviews/product/${productId}`);
  }

  addReview(review: any): Observable<any> {
    return this.http.post<any>('/api/reviews/save', review);
  }

  deleteReview(reviewId: number): Observable<any> {
    return this.http.delete<any>(`/api/reviews/delete/${reviewId}`);
  }
}