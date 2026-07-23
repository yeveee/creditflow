import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { CurrentUser, LoginRequest, LoginResponse, Role } from '../models/auth.model';

const TOKEN_KEY = 'creditflow_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _user = signal<CurrentUser | null>(this.decode(this.readToken()));

  readonly user = this._user.asReadonly();
  readonly role = computed<Role | null>(() => this._user()?.role ?? null);
  readonly username = computed<string | null>(() => this._user()?.username ?? null);
  readonly isAuthenticated = computed<boolean>(() => {
    const u = this._user();
    return !!u && u.exp * 1000 > Date.now();
  });

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/v1/auth/login', payload)
      .pipe(tap((res) => this.setSession(res.token)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._user.set(null);
  }

  hasRole(...roles: Role[]): boolean {
    const r = this.role();
    return !!r && roles.includes(r);
  }

  get token(): string | null {
    return this.readToken();
  }

  private readToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private setSession(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this._user.set(this.decode(token));
  }

  private decode(token: string | null): CurrentUser | null {
    if (!token) {
      return null;
    }
    try {
      const parts = token.split('.');
      if (parts.length < 2) {
        return null;
      }
      const payload = JSON.parse(this.base64UrlDecode(parts[1]));
      if (!payload?.sub || !payload?.role || !payload?.exp) {
        return null;
      }
      return { username: payload.sub, role: payload.role as Role, exp: payload.exp };
    } catch {
      return null;
    }
  }

  private base64UrlDecode(input: string): string {
    const base64 = input.replace(/-/g, '+').replace(/_/g, '/');
    const padding = (4 - (base64.length % 4)) % 4;
    const padded = base64 + '='.repeat(padding);
    const decoded = atob(padded);
    return decodeURIComponent(
      decoded
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
  }
}
