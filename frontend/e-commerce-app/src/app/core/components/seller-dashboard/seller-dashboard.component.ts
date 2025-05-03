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
  isProcessingDelivery = false;
  processingOrderId: number | null = null;
  loadingOrderDetails = false;

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
        // Process the orders
        this.customerOrders = orders.map(order => {
          const sellerItems = order.items.filter((item: any) =>
            item.product.seller.userId === sellerId
          );

          // Database'den gelen status değerini güvenli bir şekilde al
          const orderStatus = order.orderStatus || order.status || 'PENDING';

          // Her iki alan için de güncel değeri kullan (UI uyumluluğu için)
          return {
            id: order.orderId,
            orderDate: new Date(order.orderDate),
            status: orderStatus,         // Eski UI referansları için
            orderStatus: orderStatus,    // Doğrudan DB alanı
            sellerApproved: sellerItems.every((item: any) =>
              item.itemStatus === 'SHIPPED' || item.itemStatus === 'DELIVERED'
            ),
            isMultiSellerOrder: order.items.some((item: any) =>
              item.product.seller.userId !== sellerId
            ),
            isDelivered: orderStatus === 'DELIVERED',
            customer: {
              id: order.user?.userId,
              name: `${order.user?.firstName || ''} ${order.user?.lastName || ''}`.trim() || 'Unknown',
              email: order.user?.email || order.email || 'No email provided'
            },
            items: sellerItems.map((item: any) => ({
              productId: item.product.productId,
              productName: item.product.productName,
              quantity: item.quantityInOrder,
              price: item.product.price,
              subtotal: item.orderedProductPrice,
              status: item.itemStatus || (orderStatus === 'DELIVERED' ? 'DELIVERED' : 'PENDING')
            })),
            totalPrice: sellerItems.reduce(
              (sum: number, item: any) => sum + (item.orderedProductPrice || 0),
              0
            )
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
          this.customerOrders[idx].sellerApproved = true;

          if (updatedOrder.orderStatus === 'SHIPPED') {
            this.customerOrders[idx].status = 'SHIPPED';
            this.successMessage = 'All items in this order have been shipped!';
          } else {
            this.customerOrders[idx].status = 'PARTIALLY_SHIPPED';
            this.customerOrders[idx].sellerShipped = true;
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
    if (!confirm('Are you sure you want to cancel this order? This will:\n- Return items to inventory\n- Process a refund to the customer\n- Deduct any earnings you\'ve received')) {
      return;
    }

    this.isProcessing = true;

    this.orderService.refundAndCancelOrder(orderId, 'seller').subscribe({
      next: () => {
        const index = this.customerOrders.findIndex(o => o.id === orderId);
        if (index !== -1) {
          this.customerOrders[index].status = 'CANCELLED';
        }

        this.successMessage = 'Order cancelled successfully. Products have been returned to inventory and customer has been refunded.';
        this.isProcessing = false;

        setTimeout(() => {
          this.loadSellerOrders();
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        console.error('Error cancelling order', error);
        this.errorMessage = error.message || 'Failed to cancel order. Please try again.';
        this.isProcessing = false;
        setTimeout(() => this.errorMessage = '', 3000);
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
    if (this.expandedOrderId === orderId) {
      this.expandedOrderId = null;
    } else {
      this.loadingOrderDetails = true;

      // Önbellek sorunlarını önlemek için timestamp ekleyerek zorla yenileme
      this.orderService.getOrderById(orderId, true).subscribe({
        next: (orderDetails) => {
          console.log('Raw order details from backend:', JSON.stringify(orderDetails, null, 2));

          const index = this.customerOrders.findIndex(order => order.id === orderId);
          if (index !== -1) {
            const sellerId = this.getCurrentSellerId();
            const sellerItems = orderDetails.items?.filter((item: any) =>
              item.product?.seller?.userId === sellerId
            ) || [];

            // Backend'den gelen orderStatus/status değerine öncelik ver
            const dbOrderStatus = orderDetails.orderStatus || orderDetails.status;

            if (dbOrderStatus) {
              // Önemli: Her iki alanı da güncelliyoruz (UI uyumluluğu için)
              this.customerOrders[index].orderStatus = dbOrderStatus;
              this.customerOrders[index].status = dbOrderStatus;
              console.log(`Order ${orderId} status güncellendi:`, dbOrderStatus);
            } else {
              console.warn('Backend response does not contain order status!');
            }

            // ItemStatus alanlarını güncelleme
            this.customerOrders[index].items = sellerItems.map((item: any) => {
              return {
                productId: item.product.productId,
                productName: item.product.productName,
                quantity: item.quantityInOrder,
                price: item.product.price,
                subtotal: item.orderedProductPrice,
                status: item.itemStatus || item.status ||
                        (dbOrderStatus === 'DELIVERED' ? 'DELIVERED' : 'PENDING')
              };
            });
          }

          this.expandedOrderId = orderId;
          this.loadingOrderDetails = false;
        },
        error: (error) => {
          console.error('Error loading order details', error);
          this.loadingOrderDetails = false;
          this.errorMessage = 'Failed to load order details. Please try again.';
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  private getCurrentSellerId(): number | null {
    const currentUser = this.authService.getCurrentUser();
    return currentUser ? currentUser.userId : null;
  }

  markAsDelivered(orderId: number): void {
    if (!confirm('Are you sure you want to mark this order as delivered?')) {
      return;
    }

    this.isProcessingDelivery = true;
    this.processingOrderId = orderId;

    this.orderService.updateOrderStatus(orderId, 'DELIVERED').subscribe({
      next: () => {
        const index = this.customerOrders.findIndex(order => order.id === orderId);
        if (index !== -1) {
          this.customerOrders[index].status = 'DELIVERED';
          this.customerOrders[index].isDelivered = true;

          if (this.customerOrders[index].items) {
            this.customerOrders[index].items.forEach((item: { status: string }) => {
              item.status = 'DELIVERED';
            });
          }
        }

        this.successMessage = 'Order has been marked as delivered! The customer has been notified.';
        this.isProcessingDelivery = false;
        this.processingOrderId = null;

        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        console.error('Error marking order as delivered:', error);
        this.errorMessage = error.message || 'Failed to mark order as delivered. Please try again.';
        this.isProcessingDelivery = false;
        this.processingOrderId = null;

        setTimeout(() => {
          this.errorMessage = '';
        }, 3000);
      }
    });
  }

  getOrderStatusText(order: any): string {
    if (order.status === 'CANCELLED') {
      return 'CANCELLED';
    } else if (order.status === 'DELIVERED') {
      return 'DELIVERED';
    } else if (order.sellerApproved) {
      return 'YOU SHIPPED';
    } else {
      return 'PENDING';
    }
  }
}
