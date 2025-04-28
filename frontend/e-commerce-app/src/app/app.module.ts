// src/app/app.module.ts  📦
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FlexLayoutModule } from '@angular/flex-layout';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CustomerComponent } from './customer/customer.component';
import { CartPageComponent } from './cart-page/cart-page.component';

@NgModule({
  declarations: [AppComponent, ProductListComponent, LoginComponent, SignupComponent, CustomerComponent, CartPageComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FlexLayoutModule,

    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    ReactiveFormsModule,

    AppRoutingModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
