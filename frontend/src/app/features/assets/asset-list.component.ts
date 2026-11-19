import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Asset, Category, Page, UnitLocation } from '../../core/models/models';

@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, RouterLink],
  template: `
  <h1>Asset catalogue</h1>
  <div class="ui-card filters">
    <input [(ngModel)]="q" placeholder="Search tag, name, brand, model, serial (FR-03)">
    <select [(ngModel)]="status"><option value="">All statuses</option>
      <option *ngFor="let s of statuses" [value]="s">{{ s }}</option></select>
    <select [(ngModel)]="categoryId"><option value="">All categories</option>
      <option *ngFor="let c of categories()" [value]="c.id">{{ c.name }}</option></select>
    <button class="btn btn-primary btn-sm" (click)="load()">Search</button>
  </div>
  <div class="ui-card table-card">
    <table>
      <thead><tr><th>Asset tag / QR</th><th>Name</th><th>Category</th><th>Unit</th><th>Status</th><th></th></tr></thead>
      <tbody>
        <tr *ngFor="let a of page()?.content">
          <td><strong>{{ a.assetTag }}</strong></td>
          <td>{{ a.name }}</td>
          <td>{{ a.categoryName }}</td>
          <td>{{ a.locationUnit }}</td>
          <td><span class="badge badge-type">{{ a.status }}</span></td>
          <td><a [routerLink]="['/assets', a.id]">Open</a></td>
        </tr>
      </tbody>
    </table>
    <div class="empty" *ngIf="page() && page()!.content.length === 0">No records match these filters.</div>
  </div>
  <div class="ui-card pad register" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER')">
    <h2>Register asset (FR-02)</h2>
    <div class="form-grid">
      <input [(ngModel)]="form.assetTag" placeholder="Asset tag e.g. UI-ITEMS-010">
      <input [(ngModel)]="form.name" placeholder="Name / description">
      <select [(ngModel)]="form.categoryId"><option [ngValue]="null">Category…</option>
        <option *ngFor="let c of categories()" [ngValue]="c.id">{{ c.name }}</option></select>
      <select [(ngModel)]="form.locationId"><option [ngValue]="null">Location…</option>
        <option *ngFor="let l of locations()" [ngValue]="l.id">{{ l.unitName }} — {{ l.building }}</option></select>
      <input [(ngModel)]="form.brand" placeholder="Brand">
      <input [(ngModel)]="form.model" placeholder="Model">
      <input [(ngModel)]="form.serialNumber" placeholder="Serial (optional)">
    </div>
    <div class="err" *ngIf="error()">{{ error() }}</div>
    <button class="btn btn-gold btn-sm" (click)="register()">Register</button>
  </div>`,
  styles: [`
    .filters { display:flex; gap:8px; padding:14px; margin:12px 0; flex-wrap:wrap; }
    .filters input, .filters select { padding:9px 10px; border:1px solid var(--border-color);
      border-radius:var(--radius-sm); font-size:0.88rem; }
    .filters input { flex:1; min-width:220px; }
    .table-card { overflow:auto; } table { width:100%; border-collapse:collapse; font-size:0.88rem; }
    th, td { text-align:left; padding:10px 12px; border-bottom:1px solid var(--border-color); }
    th { background:#F1F5F9; font-size:0.75rem; text-transform:uppercase; letter-spacing:0.04em; }
    .empty { padding:18px; color:var(--text-muted); }
    .pad { padding:18px; margin-top:14px; } h2 { font-size:1rem; margin-bottom:10px; }
    .form-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:8px; margin-bottom:10px; }
    .form-grid input, .form-grid select { padding:9px 10px; border:1px solid var(--border-color); border-radius:var(--radius-sm); }
    .err { color:var(--ui-crimson); font-size:0.85rem; margin-bottom:8px; }
  `]
})
export class AssetListComponent implements OnInit {
  q = '';
  status = '';
  categoryId: any = '';
  categories = signal<Category[]>([]);
  locations = signal<UnitLocation[]>([]);
  page = signal<Page<Asset> | null>(null);
  error = signal<string | null>(null);
  form: any = { assetTag: '', name: '', categoryId: null, locationId: null, brand: '', model: '', serialNumber: '' };
  statuses = ['REGISTERED', 'AVAILABLE', 'ASSIGNED', 'UNDER_MAINTENANCE', 'RETIRED', 'DISPOSED'];

  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void {
    this.api.get<Category[]>('/reference/categories').subscribe({ next: (c) => this.categories.set(c) });
    this.api.get<UnitLocation[]>('/reference/locations').subscribe({ next: (l) => this.locations.set(l) });
    this.load();
  }

  load(): void {
    this.api.get<Page<Asset>>('/assets', {
      query: this.q, status: this.status || undefined,
      categoryId: this.categoryId || undefined, size: 50
    }).subscribe({ next: (p) => this.page.set(p) });
  }

  register(): void {
    this.error.set(null);
    this.api.post<Asset>('/assets', this.form).subscribe({
      next: () => { this.form = { assetTag: '', name: '', categoryId: null, locationId: null, brand: '', model: '', serialNumber: '' }; this.load(); },
      error: (e) => this.error.set(e.error?.message ?? 'Registration failed — tag must be unique (TC-02).')
    });
  }
}
