import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-oauth2-success',
    standalone: false,
    template: `
    <div class="d-flex justify-content-center align-items-center vh-100">
      <div class="text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-3">Processing your login, please wait...</p>
        <div *ngIf="errorMessage" class="alert alert-danger mt-3">
          {{ errorMessage }}
          <div class="mt-2">
            <a routerLink="/login" class="btn btn-outline-primary btn-sm">Return to login</a>
          </div>
        </div>
      </div>
    </div>
  `
})
export class OAuth2SuccessComponent implements OnInit {
    errorMessage: string = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        // Get token from URL parameter
        this.route.queryParams.subscribe(params => {
            const token = params['token'];
            console.log('Received OAuth2 token:', token ? 'yes' : 'no');

            if (token) {
                // Process OAuth2 login
                this.authService.handleOAuth2Success(token).subscribe({
                    next: (user) => {
                        console.log('OAuth2 login successful for user:', user?.email);
                        // Navigate to appropriate page based on user role
                        this.authService.handleLoginSuccess();
                    },
                    error: (error) => {
                        console.error('OAuth2 login error:', error);
                        this.errorMessage = `Authentication error: ${error.status} ${error.statusText}. 
                                            Please try again or contact support.`;
                    }
                });
            } else {
                console.log('No token found in URL');
                this.errorMessage = 'No authentication token received. Please try again.';
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 3000);
            }
        });
    }
}
