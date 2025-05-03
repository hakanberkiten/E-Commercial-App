import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { OrderService } from '../../services/order.service';
import { User, AuthService } from '../../services/auth.service';
import { CategoryService } from '../../services/category.service';
declare var bootstrap: any;

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  // Active tab tracking
  activeTab: string = 'users';

  // User management
  users: User[] = [];
  selectedUser: User | null = null;
  roles: any[] = []; // To store available roles

  // Product management
  products: any[] = [];
  selectedProduct: any = null;

  // Order management
  orders: any[] = [];

  // Messages and alerts
  successMessage: string = '';
  errorMessage: string = '';

  // Loading states
  loadingUsers: boolean = false;
  loadingProducts: boolean = false;
  loadingOrders: boolean = false;
  loadingRoles: boolean = false;

  // Role constants for easier reference
  roleIds = {
    ADMIN: 1,
    SELLER: 2,
    CUSTOMER: 3
  };

  // Product editing
  productForm!: FormGroup;
  categories: any[] = [];
  isEditing: boolean = false;

  // Password reset
  resetPasswordForm!: FormGroup;
  selectedUserId: number = 0;
  passwordResetModal: any;
  showPassword: boolean = false;
  isResettingPassword: boolean = false;
  userBeingReset: any = null;

  constructor(
    private http: HttpClient,
    private formBuilder: FormBuilder,
    private productService: ProductService,
    private orderService: OrderService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.loadAllRoles();
    this.loadAllUsers();
    this.loadAllProducts();
    this.loadAllOrders();
    this.loadCategories();

    // Initialize product form
    this.productForm = this.formBuilder.group({
      productId: [null],
      productName: ['', Validators.required],
      description: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      image: [''],
      quantityInStock: [0, [Validators.required, Validators.min(0)]],
      categoryId: [null, Validators.required]
    });

    // Initialize the resetPasswordForm
    this.resetPasswordForm = this.formBuilder.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  // Tab management
  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  // Load available roles
  loadAllRoles(): void {
    this.loadingRoles = true;
    this.http.get<any[]>('/api/roles').subscribe({
      next: (data) => {
        this.roles = data;
        this.loadingRoles = false;
        console.log('Roles loaded:', this.roles);
      },
      error: (error) => {
        console.error('Failed to load roles:', error);
        this.loadingRoles = false;
        this.errorMessage = 'Failed to load roles: ' + error.message;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  // User management methods
  loadAllUsers(): void {
    this.loadingUsers = true;
    this.http.get<User[]>('/api/users').subscribe({
      next: (data) => {
        this.users = data;
        this.loadingUsers = false;
        console.log('Users loaded:', this.users);
      },
      error: (error) => {
        this.errorMessage = 'Failed to load users: ' + error.message;
        this.loadingUsers = false;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  // Get role name by ID
  getRoleName(roleId: number): string {
    const role = this.roles.find(r => r.roleId === roleId);
    return role ? role.roleName : 'Unknown';
  }

  // Set user role
  setUserRole(user: User, roleId: number): void {
    const payload = { roleId: roleId };

    // Clear any previous messages
    this.successMessage = '';
    this.errorMessage = '';

    // Log the request for debugging
    console.log(`Setting user ${user.userId} to role ${roleId}`);

    this.http.put(`/api/users/${user.userId}/role`, payload).subscribe({
      next: (response) => {
        console.log('Role update successful:', response);
        const roleName = this.getRoleName(roleId);
        this.successMessage = `Updated ${user.firstName}'s role to ${roleName} successfully`;
        this.loadAllUsers(); // Refresh user list
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Role update failed:', error);
        this.errorMessage = `Failed to update role: ${error.message || 'Server error occurred'}`;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  toggleUserStatus(user: User): void {
    const newStatus = !user.active;

    // For debugging, log the request details
    console.log(`Toggling user ${user.userId} to ${newStatus ? 'active' : 'inactive'}`);

    this.http.patch(`/api/users/${user.userId}/status`, { active: newStatus }).subscribe({
      next: (response) => {
        console.log('Toggle status response:', response);
        this.successMessage = `User ${newStatus ? 'activated' : 'deactivated'} successfully`;
        this.loadAllUsers(); // Refresh user list
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Toggle status error:', error);
        this.errorMessage = `Failed to update user status: ${error.message}`;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  deleteUser(userId: number): void {
    if (confirm('WARNING: This will permanently delete this user and ALL associated data including:\n\n- Orders\n- Reviews\n- Shopping Cart\n- Addresses\n- Payment Methods\n- All other user data\n\nThis action CANNOT be undone. Are you sure?')) {
      this.http.delete(`/api/users/${userId}`).subscribe({
        next: () => {
          this.successMessage = 'User and all associated data deleted successfully';
          this.loadAllUsers(); // Refresh user list
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          this.errorMessage = `Failed to delete user: ${error.message || 'Unknown error'}`;
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  resetUserPassword(userId: number, userName: string = ''): void {
    this.selectedUserId = userId;
    this.userBeingReset = this.users.find(u => u.userId === userId) || { firstName: userName };
    this.resetPasswordForm.reset();
    this.isResettingPassword = true;

    // Scroll to the password reset section if needed
    setTimeout(() => {
      const resetSection = document.getElementById('password-reset-section');
      if (resetSection) {
        resetSection.scrollIntoView({ behavior: 'smooth' });
      }
    }, 100);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  submitPasswordReset(): void {
    if (this.resetPasswordForm.invalid) {
      return;
    }

    const newPassword = this.resetPasswordForm.get('newPassword')?.value;

    this.http.post(`/api/users/${this.selectedUserId}/reset-password`, { newPassword }).subscribe({
      next: () => {
        this.successMessage = 'Password has been successfully changed';
        this.isResettingPassword = false;
        this.userBeingReset = null;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = `Failed to reset password: ${error.message || 'Unknown error'}`;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  cancelPasswordReset(): void {
    this.isResettingPassword = false;
    this.userBeingReset = null;
    this.resetPasswordForm.reset();
  }

  // Product management methods
  loadAllProducts(): void {
    this.loadingProducts = true;
    this.http.get<any[]>('/api/products/all').subscribe({
      next: (data) => {
        this.products = data;
        this.loadingProducts = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load products: ' + error.message;
        this.loadingProducts = false;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  deleteProduct(productId: number): void {
    if (confirm('Are you sure you want to delete this product? This action cannot be undone.')) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.successMessage = 'Product deleted successfully';
          this.loadAllProducts(); // Refresh product list
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          this.errorMessage = `Failed to delete product: ${error.message}`;
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  loadCategories(): void {
    this.http.get<any[]>('/api/categories/all').subscribe({
      next: (data) => {
        this.categories = data;
        console.log('Categories loaded:', data); // Add this to debug
      },
      error: (error) => {
        console.error('Failed to load categories:', error); // Add more detailed logging
        this.errorMessage = 'Failed to load categories: ' + error.message;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  editProduct(product: any): void {
    this.isEditing = true;
    this.selectedProduct = product;

    this.productForm.patchValue({
      productId: product.productId,
      productName: product.productName,
      description: product.description,
      price: product.price,
      image: product.image || '',
      quantityInStock: product.quantityInStock,
      categoryId: product.category?.categoryId
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.selectedProduct = null;
    this.productForm.reset();
  }

  saveProductChanges(): void {
    if (this.productForm.invalid) {
      this.errorMessage = 'Please fill all required fields correctly';
      setTimeout(() => this.errorMessage = '', 3000);
      return;
    }

    const productData = {
      ...this.productForm.value,
      category: {
        categoryId: this.productForm.value.categoryId
      },
      seller: this.selectedProduct.seller // Keep the original seller
    };

    this.productService.updateProduct(productData).subscribe({
      next: () => {
        this.successMessage = 'Product updated successfully';
        this.loadAllProducts(); // Refresh product list
        this.cancelEdit(); // Reset form
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = `Failed to update product: ${error.message || 'Unknown error'}`;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  // Order management methods
  loadAllOrders(): void {
    this.loadingOrders = true;
    this.http.get<any[]>('/api/orders/all').subscribe({
      next: (data) => {
        // Sort orders by orderId in descending order (newest first)
        this.orders = data.sort((a, b) => b.orderId - a.orderId);
        this.loadingOrders = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load orders: ' + error.message;
        this.loadingOrders = false;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  updateOrderStatus(orderId: number, status: string): void {
    if (status === 'CANCELLED') {
      if (confirm('Are you sure you want to cancel this order? Products will be returned to stock, and a refund will be issued to the customer.')) {
        this.orderService.refundAndCancelOrder(orderId, 'admin').subscribe({
          next: () => {
            this.successMessage = 'Order has been canceled, and products have been returned to stock';
            this.loadAllOrders(); // Refresh the order list
            setTimeout(() => this.successMessage = '', 3000);
          },
          error: (error) => {
            console.error('Error cancelling order:', error);
            this.errorMessage = `Failed to cancel the order: ${error.message || 'Unknown error'}`;
            setTimeout(() => this.errorMessage = '', 3000);
          }
        });
      }
    } else {
      // For PENDING or other statuses, use the regular update method
      this.orderService.updateOrderStatus(orderId, status).subscribe({
        next: () => {
          this.successMessage = `Order status updated to ${status} successfully`;
          this.loadAllOrders(); // Refresh the order list
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error updating order status:', error);
          this.errorMessage = `Failed to update order status: ${error.message || 'Unknown error'}`;
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }
}

