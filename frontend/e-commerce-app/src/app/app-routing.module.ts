import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { CustomerComponent } from './core/components/customer/customer.component';
import { CartPageComponent } from './cart-page/cart-page.component';
import { AuthGuard } from './core/guards/auth.guard';
import { ProfileComponent } from './core/components/profile/profile.component';
import { ProductDetailComponent } from './features/products/product-detail/product-detail.component';
import { SellerDashboardComponent } from './core/components/seller-dashboard/seller-dashboard.component';
import { AdminDashboardComponent } from './core/components/admin-dashboard/admin-dashboard.component';
import { NotificationsComponent } from './core/components/notification/notification.component';
import { AdminRestrictGuard } from './core/guards/admin-restrict.guard';
import { OAuth2SuccessComponent } from './auth/oauth2/oauth2-success.component';

const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  {
    path: 'products',
    component: CustomerComponent,
    canActivate: [AdminRestrictGuard]
  },
  {
    path: 'products/:id',
    component: ProductDetailComponent,
    canActivate: [AdminRestrictGuard]
  },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'seller-dashboard',
    component: SellerDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['SELLER'] }
  },
  {
    path: 'cart',
    component: CartPageComponent,
    canActivate: [AuthGuard],
    data: { role: 'ROLE_CUSTOMER' }
  },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }  // This format works with our updated guard
  },
  {
    path: 'notifications',
    component: NotificationsComponent,
    canActivate: [AuthGuard]
  },
  { path: 'auth/oauth2/success', component: OAuth2SuccessComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
