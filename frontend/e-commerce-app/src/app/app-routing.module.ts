// src/app/app-routing.module.ts — Yeni route’lar
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { CustomerComponent } from './customer/customer.component';
import { CartPageComponent } from './cart-page/cart-page.component';

const routes: Routes = [
  { path: '', redirectTo: 'signup', pathMatch: 'full' },
  { path: 'products', component: ProductListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'customer', component: CustomerComponent },
  { path: 'cart', component: CartPageComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
