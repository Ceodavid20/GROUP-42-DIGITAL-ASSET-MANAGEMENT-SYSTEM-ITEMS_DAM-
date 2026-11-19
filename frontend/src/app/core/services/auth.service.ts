import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = '/api/v1/auth';
  user = signal<LoginResponse | null>(this.restore());

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, { username, password }).pipe(
      tap((u) => {
        localStorage.setItem('dam_token', u.token);
        localStorage.setItem('dam_user', JSON.stringify(u));
        this.user.set(u);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('dam_token');
    localStorage.removeItem('dam_user');
    this.user.set(null);
  }

  token(): string | null {
    return localStorage.getItem('dam_token');
  }

  hasRole(...roles: string[]): boolean {
    const u = this.user();
    return !!u && roles.some((r) => u.roles.includes(r));
  }

  private restore(): LoginResponse | null {
    try {
      const raw = localStorage.getItem('dam_user');
      return raw ? (JSON.parse(raw) as LoginResponse) : null;
    } catch {
      return null;
    }
  }
}
