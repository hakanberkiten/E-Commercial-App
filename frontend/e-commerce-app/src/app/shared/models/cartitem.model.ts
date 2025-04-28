import { Product } from "./product.model";

export interface CartItem {
    /** Veritabanındaki PRIMARY KEY */
    cartItemId: number;
    /** Sepete eklenen ürün */
    product: Product;
    /** Sepete eklenen adet */
    quantityInCart: number;
}