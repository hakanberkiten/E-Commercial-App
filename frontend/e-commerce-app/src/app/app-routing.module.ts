import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { CustomerComponent } from './customer/customer.component';
import { CartPageComponent } from './cart-page/cart-page.component';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  {
    path: 'products',
    component: CustomerComponent,
    canActivate: [AuthGuard],
    data: { role: 'ROLE_CUSTOMER' }
  },
  {
    path: 'cart',
    component: CartPageComponent,
    canActivate: [AuthGuard],
    data: { role: 'ROLE_CUSTOMER' }
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }