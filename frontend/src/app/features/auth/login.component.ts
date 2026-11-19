import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf],
  template: `
  <div class="login-wrap">
    <div class="ui-card login-card">
      <div class="crest-lg">UI</div>
      <h1>ITeMS Asset Management</h1>
      <p class="sub">Sign in with your institutional account.</p>
      <label>Username
        <input [(ngModel)]="username" placeholder="e.g. admin" autocomplete="username">
      </label>
      <label>Password
        <input [(ngModel)]="password" type="password" placeholder="••••••••" autocomplete="current-password">
      </label>
      <div class="hint">
        admin/admin123 &middot; officer/officer123 &middot; custodian/custodian123<br>
        technician/technician123 &middot; auditor/auditor123
      </div>
      <div class="err" *ngIf="error()">{{ error() }}</div>
      <button class="btn btn-gold" (click)="submit()" [disabled]="busy()">Sign in</button>
    </div>
  </div>`,
  styles: [`
    .login-wrap { display:flex; justify-content:center; padding:48px 16px; }
    .login-card { max-width:420px; width:100%; padding:32px; text-align:center; }
    .crest-lg { width:56px; height:56px; margin:0 auto 12px; border-radius:50%;
      background:var(--ui-navy); color:var(--ui-gold); font-weight:800; font-size:1.4rem;
      display:flex; align-items:center; justify-content:center; }
    h1 { font-size:1.3rem; }
    .sub { color:var(--text-secondary); font-size:0.88rem; margin:6px 0 18px; }
    label { display:block; text-align:left; font-size:0.82rem; font-weight:600; margin-bottom:12px; }
    input { width:100%; margin-top:4px; padding:10px 12px; border:1px solid var(--border-color);
      border-radius:var(--radius-sm); font-size:0.92rem; }
    .hint { font-size:0.75rem; color:var(--text-muted); margin:8px 0 14px; line-height:1.6; }
    .err { color:var(--ui-crimson); font-size:0.85rem; margin-bottom:10px; }
    .btn { width:100%; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  error = signal<string | null>(null);
  busy = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error.set(null);
    this.busy.set(true);
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.busy.set(false);
        this.router.navigate(['/']);
      },
      error: () => {
        this.busy.set(false);
        this.error.set('Invalid credentials rejected (FR-01). Check the username and password and try again.');
      }
    });
  }
}
