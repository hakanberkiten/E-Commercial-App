import { Component } from '@angular/core';
import { HeaderComponent } from './components/header/header.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, NavbarComponent, RouterModule], // ✅ Ensure RouterModule is imported
  template: `
    <app-header></app-header>
    <app-navbar></app-navbar>
    <router-outlet></router-outlet>
  `,
})
export class AppComponent { }
