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
        this.customerOrders = orders;
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

    // Map form values to backend model property names
    const productData = {
      productId: this.productForm.value.id,
      productName: this.productForm.value.name,
      description: this.productForm.value.description,
      price: this.productForm.value.price,
      quantityInStock: this.productForm.value.stockQuantity,
      image: this.productForm.value.imageUrl,
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
        },
        error: (error) => {
          console.error('Error updating product', error);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: () => {
          this.loadSellerProducts();
          this.addNewProduct();
        },
        error: (error) => {
          console.error('Error creating product', error);
        }
      });
    }
  }

  approveOrder(orderId: number) {
    this.orderService.updateOrderStatus(orderId, 'APPROVED').subscribe({
      next: () => {
        this.loadSellerOrders();
      },
      error: (error) => {
        console.error('Error approving order', error);
      }
    });
  }

  cancelOrder(orderId: number) {
    this.orderService.updateOrderStatus(orderId, 'CANCELLED').subscribe({
      next: () => {
        this.loadSellerOrders();
      },
      error: (error) => {
        console.error('Error cancelling order', error);
      }
    });
  }

  // Add this method to your SellerDashboardComponent
  deleteProduct(product: any) {
    if (confirm(`Are you sure you want to delete ${product.name}? This will also remove all related reviews and cannot be undone.`)) {
      // Make sure you're using the correct ID property from your transformed data
      const productId = product.id || product.productId;

      console.log('Deleting product with ID:', productId);

      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.loadSellerProducts();
          alert('Product deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          alert(`Error deleting product: ${error.status === 403 ? 'Permission denied' : error.message || 'Unknown error'}`);
        }
      });
    }
  }
}