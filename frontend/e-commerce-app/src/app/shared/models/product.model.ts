// src/app/shared/models/product.model.ts
export interface Product {
  productId: number;
  productName: string;
  description: string;
  price: number;
  image?: string;
  quantityInStock: number;
  reviewCount?: number;
  productRate?: number;

}
