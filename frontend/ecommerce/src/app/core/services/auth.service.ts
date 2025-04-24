import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

interface LoginRes { token: string; type: string; }
interface LoginReq { username: string; password: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
    private api = `${environment.apiUrl}/auth`;
    constructor(private http: HttpClient) { }
    login(username: string, password: string) {
        return this.http.post<LoginRes>(`${this.api}/login`, { username, password })
            .pipe(tap(res => localStorage.setItem('jwt', res.token)));
    }
    register(data: any) {
        return this.http.post(`${this.api}/register`, data);
    }
    logout() { localStorage.removeItem('jwt'); }
    get token() { return localStorage.getItem('jwt'); }
    get isLoggedIn() { return !!this.token; }
}
