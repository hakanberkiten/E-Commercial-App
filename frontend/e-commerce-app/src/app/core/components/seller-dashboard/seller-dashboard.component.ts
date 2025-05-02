// src/app/seller/dashboard/seller-dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { CategoryService } from '../../services/category.service';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { Category } from '../../../shared/models/category.model';

@Component({
  selector: 'app-seller-dashboard',
  standalone: false,
  templateUrl: './seller-dashboard.component.html',
  styleUrls: ['./seller-dashboard.component.css']
})
export class SellerDashboardComponent implements OnInit {
  sellerProducts: any[] = [];
  selectedProduct: any = null;
  customerOrders: any[] = [];
  productForm: FormGroup;
  isEditing = false;

  categories: Category[] = [];
  selectedCategoryId: number | null = null;
  isLoading = false;
  error: string | null = null;
  productSuccessMessage = '';
  productErrorMessage = '';
  expandedOrderId: number | null = null;
  isProcessing = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private categoryService: CategoryService,
    private productService: ProductService,
    private orderService: OrderService,
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router
  ) {
    this.productForm = this.formBuilder.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0.01)]],
      stockQuantity: ['', [Validators.required, Validators.min(0)]],
      imageUrl: [''],
      category: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadSellerProducts();
    this.loadSellerOrders();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.error = null;

    this.categoryService.getAll().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoading = false;
        console.log('Categories loaded:', this.categories);
      },
      error: (err) => {
        this.error = 'Failed to load categories. Please try again.';
        this.isLoading = false;
        console.error('Error loading categories:', err);
      }
    });
  }

  onCategoryChange(categoryId: string): void {
    this.selectedCategoryId = categoryId ? parseInt(categoryId, 10) : null;
  }

  loadSellerProducts(): void {
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }

    this.productService.getProductsBySellerId(sellerId).subscribe({
      next: (products) => {
        this.sellerProducts = products.map(product => ({
          id: product.productId,
          name: product.productName,
          description: product.description,
          price: product.price,
          stockQuantity: product.quantityInStock,
          imageUrl: product.image,
          category: product.category,
          ...product
        }));
      },
      error: (error) => {
        console.error('Error loading seller products', error);
      }
    });
  }

  loadSellerOrders(): void {
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }

    this.orderService.getOrdersBySellerId(sellerId).subscribe({
      next: (orders) => {
        const activeOrders = orders.filter(o => o.orderStatus !== 'CANCELLED');
        this.customerOrders = activeOrders.map(order => {
          const sellerItems = order.items.filter((item: any) =>
            item.product.seller.userId === sellerId
          );
          const sellerTotal = sellerItems.reduce(
            (sum: number, item: any) => sum + (item.orderedProductPrice || 0),
            0
          );
          // Check if this seller's items are already shipped
          const sellerApproved = sellerItems.every((item: any) =>
            item.itemStatus === 'SHIPPED'
          );

          // Check if this is a multi-seller order
          const isMultiSellerOrder = order.items.some((item: any) =>
            item.product.seller.userId !== sellerId
          );

          return {
            id: order.orderId,
            orderDate: new Date(order.orderDate),
            status: order.orderStatus,
            sellerApproved,
            sellerShipped: sellerApproved, // Track if this seller's items are shipped
            isMultiSellerOrder,
            customer: {
              id: order.user?.userId,
              name: `${order.user?.firstName || ''} ${order.user?.lastName || ''}`.trim() || 'Unknown',
              email: order.user?.email || order.email || 'No email provided'
            },
            items: order.items.map((item: any) => ({
              productId: item.product.productId,
              productName: item.product.productName,
              quantity: item.quantityInOrder,
              price: item.product.price,
              subtotal: item.orderedProductPrice,
              status: item.itemStatus || 'PENDING',
              isThisSellersItem: item.product.seller.userId === sellerId
            })),
            totalPrice: sellerTotal
          };
        });
      },
      error: (error) => {
        console.error('Error loading seller orders', error);
      }
    });
  }

  editProduct(product: any): void {
    this.isEditing = true;
    this.selectedProduct = product;
    this.productForm.patchValue({
      id: product.productId,
      name: product.productName,
      description: product.description,
      price: product.price,
      stockQuantity: product.quantityInStock,
      imageUrl: product.image,
      category: product.category.categoryId
    });
  }

  addNewProduct(): void {
    this.isEditing = false;
    this.selectedProduct = null;
    this.productForm.reset();
  }

  saveProduct(): void {
    if (this.productForm.invalid) {
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }

    const imageUrl = this.productForm.value.imageUrl?.trim()
      ? this.productForm.value.imageUrl
      : 'https://i.imgur.com/xzEfMjC.png';

    const productData = {
      productId: this.productForm.value.id,
      productName: this.productForm.value.name,
      description: this.productForm.value.description,
      price: this.productForm.value.price,
      quantityInStock: this.productForm.value.stockQuantity,
      image: imageUrl,
      category: { categoryId: this.productForm.value.category },
      seller: {
        userId: sellerId,
        role: { roleId: 2 }
      }
    };

    if (this.isEditing) {
      this.productService.updateProduct(productData).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.addNewProduct();
          this.productSuccessMessage = 'Product updated successfully!';
          setTimeout(() => (this.productSuccessMessage = ''), 3000);
        },
        error: (error) => {
          console.error('Error updating product', error);
          this.productErrorMessage = 'Failed to update product. Please try again.';
          setTimeout(() => (this.productErrorMessage = ''), 3000);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.addNewProduct();
          this.productSuccessMessage = 'Product added successfully!';
          setTimeout(() => (this.productSuccessMessage = ''), 3000);
        },
        error: (error) => {
          console.error('Error creating product', error);
          this.productErrorMessage = 'Failed to add product. Please try again.';
          setTimeout(() => (this.productErrorMessage = ''), 3000);
        }
      });
    }
  }

  approveOrder(orderId: number): void {
    if (!confirm('Are you sure you want to approve and ship your items in this order?')) {
      return;
    }
    this.isProcessing = true;

    this.orderService.approveSellerItems(orderId).subscribe({
      next: (updatedOrder) => {
        const idx = this.customerOrders.findIndex(o => o.id === orderId);
        if (idx >= 0) {
          // Mark this seller's portion as approved regardless of other sellers
          this.customerOrders[idx].sellerApproved = true;

          // Update the local status to show shipping progress
          if (updatedOrder.orderStatus === 'SHIPPED') {
            this.customerOrders[idx].status = 'SHIPPED';
            this.successMessage = 'All items in this order have been shipped!';
          } else {
            // This is the key change - show the seller their part is shipped
            this.customerOrders[idx].status = 'PARTIALLY_SHIPPED';
            this.customerOrders[idx].sellerShipped = true; // New flag to track this seller's shipment
            this.successMessage = 'Your items have been approved and marked for shipping! The customer will be notified.';
          }
        }
        this.isProcessing = false;
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (error) => {
        console.error('Error approving order', error);
        this.errorMessage =
          'You need to add a payment method in your profile before approving orders.';
        this.router.navigate(['/profile'], { queryParams: { tab: 'payment' } });
        this.isProcessing = false;
        setTimeout(() => (this.errorMessage = ''), 3000);
      }
    });
  }

  cancelOrder(orderId: number): void {
    if (!confirm('Are you sure you want to cancel this order? The customer will receive a refund.')) {
      return;
    }
    this.isProcessing = true;

    this.orderService.refundAndCancelOrder(orderId).subscribe({
      next: () => {
        this.customerOrders = this.customerOrders.filter(o => o.id !== orderId);
        this.successMessage = 'Order cancelled and payment refunded to customer!';
        this.isProcessing = false;
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (error) => {
        console.error('Error cancelling order', error);
        this.errorMessage = 'Failed to cancel order. Please try again.';
        this.isProcessing = false;
        setTimeout(() => (this.errorMessage = ''), 3000);
      }
    });
  }

  deleteProduct(product: any): void {
    const confirmMsg = `Are you sure you want to delete "${product.productName || product.name}"?
This will also remove all related reviews and cannot be undone.`;
    if (!confirm(confirmMsg)) {
      return;
    }

    this.productService.deleteProduct(product.productId).subscribe({
      next: () => {
        this.loadSellerProducts();
        this.productSuccessMessage = 'Product deleted successfully!';
        setTimeout(() => (this.productSuccessMessage = ''), 3000);
      },
      error: (error) => {
        console.error('Error deleting product:', error);
        this.productErrorMessage = `Error deleting product: ${error.status === 403 ? 'Permission denied' : error.message || 'Unknown error'
          }`;
        setTimeout(() => (this.productErrorMessage = ''), 3000);
      }
    });
  }

  toggleOrderDetails(orderId: number): void {
    this.expandedOrderId = this.expandedOrderId === orderId ? null : orderId;
  }
}
