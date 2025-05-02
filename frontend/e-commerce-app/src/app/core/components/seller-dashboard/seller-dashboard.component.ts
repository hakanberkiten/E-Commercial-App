import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../../shared/models/category.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';

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
  productSuccessMessage: string = '';
  productErrorMessage: string = '';
  expandedOrderId: number | null = null;
  isProcessing = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private categoryService: CategoryService,
    private productService: ProductService,
    private orderService: OrderService,
    private authService: AuthService,
    private formBuilder: FormBuilder
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

  loadSellerProducts() {
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }
    this.productService.getProductsBySellerId(sellerId).subscribe({
      next: (products) => {
        // Transform backend model to frontend model
        this.sellerProducts = products.map(product => ({
          id: product.productId,
          name: product.productName,
          description: product.description,
          price: product.price,
          stockQuantity: product.quantityInStock,
          imageUrl: product.image,
          category: product.category,
          // Keep the original data too if needed for backend operations
          ...product
        }));
      },
      error: (error) => {
        console.error('Error loading seller products', error);
      }
    });
  }

  loadSellerOrders() {
    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }

    this.orderService.getOrdersBySellerId(sellerId).subscribe({
      next: (orders) => {
        // Filter out canceled orders
        const activeOrders = orders.filter(order => order.orderStatus !== 'CANCELLED');

        // Transform the remaining active orders into a format easier to display
        this.customerOrders = activeOrders.map(order => {
          // Calculate total for this seller's products in the order
          const sellerTotal = order.items.reduce((sum: number, item: any) =>
            sum + (item.orderedProductPrice || 0), 0);

          // Check if all of this seller's items are approved
          const sellerItems = order.items.filter((item: any) =>
            item.product.seller.userId === sellerId);

          const sellerApproved = sellerItems.every((item: any) =>
            item.itemStatus === 'SHIPPED');

          return {
            id: order.orderId,
            orderDate: new Date(order.orderDate),
            status: order.orderStatus,
            sellerApproved: sellerApproved,
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
              status: item.itemStatus || 'PENDING'
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

  editProduct(product: any) {
    this.isEditing = true;
    this.selectedProduct = product;
    this.productForm.patchValue({
      id: product.productId,           // Changed from id to productId
      name: product.productName,       // Changed from name to productName
      description: product.description, // This one matches
      price: product.price,            // This one matches
      stockQuantity: product.quantityInStock, // Changed from stockQuantity to quantityInStock
      imageUrl: product.image,         // Changed from imageUrl to image
      category: product.category.categoryId  // Changed from category.id to category.categoryId
    });
  }
  addNewProduct() {
    this.isEditing = false;
    this.selectedProduct = null;
    this.productForm.reset();
  }

  saveProduct() {
    if (this.productForm.invalid) {
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    const sellerId = currentUser ? currentUser.userId : null;
    if (!sellerId) {
      console.error('Error: Seller ID is null');
      return;
    }

    // Get the image URL from form or use default if empty
    const imageUrl = this.productForm.value.imageUrl?.trim()
      ? this.productForm.value.imageUrl
      : 'https://i.imgur.com/xzEfMjC.png';

    // Map form values to backend model property names
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
        role: { roleId: 2 } // Add this line with the seller role ID
      }
    };

    if (this.isEditing) {
      this.productService.updateProduct(productData).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.addNewProduct();
          this.productSuccessMessage = 'Product updated successfully!';
          setTimeout(() => this.productSuccessMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error updating product', error);
          this.productErrorMessage = 'Failed to update product. Please try again.';
          setTimeout(() => this.productErrorMessage = '', 3000);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.addNewProduct();
          this.productSuccessMessage = 'Product added successfully!';
          setTimeout(() => this.productSuccessMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error creating product', error);
          this.productErrorMessage = 'Failed to add product. Please try again.';
          setTimeout(() => this.productErrorMessage = '', 3000);
        }
      });
    }
  }

  approveOrder(orderId: number) {
    if (confirm('Are you sure you want to approve and ship your items in this order?')) {
      this.isProcessing = true;
      this.orderService.approveSellerItems(orderId).subscribe({
        next: (updatedOrder) => {
          // Find the order in the current list
          const orderIndex = this.customerOrders.findIndex(order => order.id === orderId);
          if (orderIndex >= 0) {
            // Update just the status display for this seller
            this.customerOrders[orderIndex].sellerApproved = true;

            // If all items are now approved, update to shipped
            if (updatedOrder.orderStatus === 'SHIPPED') {
              this.customerOrders[orderIndex].status = 'SHIPPED';
            } else {
              this.customerOrders[orderIndex].status = 'PARTIALLY_SHIPPED';
            }
          }

          this.successMessage = 'Your items have been approved and marked for shipping!';
          this.isProcessing = false;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error approving order', error);
          this.errorMessage = 'Failed to approve items. Please try again.';
          this.isProcessing = false;
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  cancelOrder(orderId: number) {
    if (confirm('Are you sure you want to cancel this order? The customer will receive a refund.')) {
      this.isProcessing = true;
      this.orderService.refundAndCancelOrder(orderId).subscribe({
        next: () => {
          // Remove the canceled order from the local array instead of reloading
          this.customerOrders = this.customerOrders.filter(order => order.id !== orderId);
          this.successMessage = 'Order cancelled and payment refunded to customer!';
          this.isProcessing = false;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error cancelling order', error);
          this.errorMessage = 'Failed to cancel order. Please try again.';
          this.isProcessing = false;
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  deleteProduct(product: any) {
    if (confirm(`Are you sure you want to delete ${product.productName || product.name}? This will also remove all related reviews and cannot be undone.`)) {
      // Use only productId since that's what the backend expects (not id)
      const productId = product.productId;

      console.log('Deleting product with ID:', productId);

      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.productSuccessMessage = 'Product deleted successfully!';
          setTimeout(() => this.productSuccessMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          this.productErrorMessage = `Error deleting product: ${error.status === 403 ? 'Permission denied' : error.message || 'Unknown error'}`;
          setTimeout(() => this.productErrorMessage = '', 3000);
        }
      });
    }
  }

  toggleOrderDetails(orderId: number): void {
    if (this.expandedOrderId === orderId) {
      this.expandedOrderId = null;
    } else {
      this.expandedOrderId = orderId;
    }
  }
}
