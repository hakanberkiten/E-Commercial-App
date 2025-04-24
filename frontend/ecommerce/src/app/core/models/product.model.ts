export interface Product {
    id: number;
    name: string;
    price: number;
    productImage: string;
    description?: string;
    category?: string;
    inStock?: boolean;
}
