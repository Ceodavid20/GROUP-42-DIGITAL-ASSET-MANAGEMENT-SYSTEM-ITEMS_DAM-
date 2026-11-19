import { Component, OnInit, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [NgFor, NgIf],
  template: `
  <h1>Reports & exceptions</h1>
  <p class="lede" *ngIf="summary() as s">Extraction date: <strong>{{ s.extractionDate }}</strong> &middot; Filters: {{ s.filters }}</p>
  <div class="two-col">
    <div class="ui-card pad">
      <h2>Unverified registrations (status = REGISTERED)</h2>
      <div class="row" *ngFor="let a of unverified()?.records">
        <span><strong>{{ a.assetTag }}</strong> — {{ a.name }}</span><span>{{ a.location?.unitName }}</span>
      </div>
      <div class="empty" *ngIf="unverified() && unverified()!.records.length === 0">None.</div>
    </div>
    <div class="ui-card pad">
      <h2>Missing metadata (FR-12)</h2>
      <div class="row" *ngFor="let a of missing()?.records">
        <span><strong>{{ a.assetTag }}</strong> — {{ a.name }}</span>
        <span>{{ !a.serialNumber ? 'no serial' : '' }} {{ !a.warrantyExpiry ? 'no warranty' : '' }}</span>
      </div>
      <div class="empty" *ngIf="missing() && missing()!.records.length === 0">None.</div>
    </div>
  </div>`,
  styles: [`
    .lede { color:var(--text-secondary); margin:4px 0 14px; }
    .two-col { display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:12px; }
    .pad { padding:18px; } h2 { font-size:0.95rem; margin-bottom:8px; }
    .row { display:flex; justify-content:space-between; gap:8px; padding:8px 0;
      border-top:1px solid var(--border-color); font-size:0.86rem; }
    .empty { color:var(--text-muted); }
  `]
})
export class ReportsComponent implements OnInit {
  summary = signal<any>(null);
  unverified = signal<any>(null);
  missing = signal<any>(null);

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.get('/reports/summary').subscribe({ next: (s) => this.summary.set(s) });
    this.api.get('/reports/exceptions/unverified', { size: 50 }).subscribe({ next: (r) => this.unverified.set(r) });
    this.api.get('/reports/exceptions/missing-metadata', { size: 50 }).subscribe({ next: (r) => this.missing.set(r) });
  }
}
