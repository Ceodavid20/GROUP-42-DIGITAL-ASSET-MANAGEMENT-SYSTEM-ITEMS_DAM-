import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  template: `
  <header class="topbar">
    <div class="brand">
      <span class="crest">UI</span>
      <div>
        <strong>ITeMS Digital Asset Management</strong>
        <small>University of Ibadan &middot; ICT equipment lifecycle register</small>
      </div>
    </div>
    <nav class="nav" *ngIf="auth.user()">
      <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">Dashboard</a>
      <a routerLink="/assets" routerLinkActive="active">Assets</a>
      <a routerLink="/transfers" routerLinkActive="active">Transfers</a>
      <a routerLink="/maintenance" routerLinkActive="active">Maintenance</a>
      <a routerLink="/reports" routerLinkActive="active">Reports</a>
      <a routerLink="/audit" routerLinkActive="active"
         *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER','ROLE_AUDITOR')">Audit</a>
    </nav>
    <div class="userbox" *ngIf="auth.user() as u">
      <span class="who">{{ u.username }}</span>
      <span class="roles">{{ u.roles.join(', ') }}</span>
      <button class="btn btn-sm btn-secondary" (click)="logout()">Sign out</button>
    </div>
  </header>
  <main class="page">
    <router-outlet></router-outlet>
  </main>
  <footer class="footer">
    ITeMS Digital Asset Management &middot; University of Ibadan
  </footer>
  `,
  styles: [`
    .topbar { display:flex; align-items:center; gap:24px; padding:12px 24px;
      background:var(--ui-navy); color:#fff; }
    .brand { display:flex; align-items:center; gap:12px; }
    .crest { width:40px; height:40px; border-radius:50%; background:var(--ui-gold);
      color:var(--ui-navy-900); display:flex; align-items:center; justify-content:center;
      font-weight:800; font-family:'Outfit',sans-serif; }
    .brand small { display:block; color:#CBD5E1; font-size:0.72rem; }
    .nav { display:flex; gap:4px; margin-left:auto; }
    .nav a { color:#E2E8F0; text-decoration:none; padding:8px 12px; border-radius:6px; font-size:0.88rem; }
    .nav a.active, .nav a:hover { background:rgba(255,255,255,0.14); color:#fff; }
    .userbox { display:flex; align-items:center; gap:10px; font-size:0.82rem; }
    .roles { color:var(--ui-gold-light); font-size:0.72rem; }
    .page { max-width:1200px; margin:0 auto; padding:24px; min-height:70vh; }
    .footer { text-align:center; padding:16px; color:var(--text-muted); font-size:0.78rem; }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
