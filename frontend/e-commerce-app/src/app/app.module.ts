// src/app/app.module.ts  📦
import { BrowserModule } from '@angular/platform-browser';
import { Inject, NgModule, PLATFORM_ID } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FlexLayoutModule } from '@angular/flex-layout';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CustomerComponent } from './core/components/customer/customer.component';
import { CartPageComponent } from './cart-page/cart-page.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { isPlatformBrowser } from '@angular/common';
import { NavbarComponent } from './core/components/navbar/navbar.component';
import { ProfileComponent } from './core/components/profile/profile.component';
import { ProductDetailComponent } from './features/products/product-detail/product-detail.component';
import { SellerDashboardComponent } from './core/components/seller-dashboard/seller-dashboard.component';
import { AdminDashboardComponent } from './core/components/admin-dashboard/admin-dashboard.component';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';
import { FooterComponent } from './core/components/footer/footer.component';

@NgModule({
  declarations: [AppComponent, ProductListComponent, LoginComponent, SignupComponent, CustomerComponent, CartPageComponent, NavbarComponent, ProfileComponent, ProductDetailComponent, SellerDashboardComponent, AdminDashboardComponent, FooterComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FlexLayoutModule,

    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [
    provideHttpClient(withFetch()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },


    // Diğer sağlayıcılar
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    const isBrowser = isPlatformBrowser(platformId);
    console.log(`Platform running in: ${isBrowser ? 'Browser' : 'Server'} mode`);
  }
}
