import { Component, OnInit, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink],
  template: `
  <h1>Inventory dashboard</h1>
  <p class="lede">Counts by status, category, and unit — extraction date shown on reports.</p>
  <div class="grid" *ngIf="summary() as s">
    <div class="ui-card stat"><span>Total assets</span><strong>{{ s.totalAssets }}</strong></div>
    <div class="ui-card stat"><span>Unverified (REGISTERED)</span><strong>{{ s.unverifiedRegistered }}</strong></div>
    <div class="ui-card stat"><span>Under maintenance</span><strong>{{ s.underMaintenance }}</strong></div>
    <div class="ui-card stat"><span>Pending transfers</span><strong>{{ s.pendingTransfers }}</strong></div>
  </div>
  <div class="two-col" *ngIf="summary() as s">
    <div class="ui-card pad">
      <h2>By status</h2>
      <div class="row" *ngFor="let k of keys(s.byStatus)"><span>{{ k }}</span><strong>{{ s.byStatus[k] }}</strong></div>
    </div>
    <div class="ui-card pad">
      <h2>By category</h2>
      <div class="row" *ngFor="let k of keys(s.byCategory)"><span>{{ k }}</span><strong>{{ s.byCategory[k] }}</strong></div>
    </div>
  </div>
  <div class="actions">
    <a class="btn btn-primary" routerLink="/assets">Open asset catalogue</a>
    <a class="btn btn-secondary" routerLink="/reports">View reports</a>
  </div>`,
  styles: [`
    .lede { color:var(--text-secondary); margin:4px 0 18px; }
    .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:12px; margin-bottom:16px; }
    .stat { padding:16px; } .stat span { font-size:0.78rem; color:var(--text-secondary); }
    .stat strong { display:block; font-size:1.6rem; color:var(--ui-navy); }
    .two-col { display:grid; grid-template-columns:repeat(auto-fit,minmax(280px,1fr)); gap:12px; }
    .pad { padding:18px; } h2 { font-size:1rem; margin-bottom:8px; }
    .row { display:flex; justify-content:space-between; padding:6px 0; border-top:1px solid var(--border-color); font-size:0.88rem; }
    .actions { display:flex; gap:10px; margin-top:18px; }
  `]
})
export class DashboardComponent implements OnInit {
  summary = signal<any>(null);

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.get<any>('/reports/summary').subscribe({ next: (s) => this.summary.set(s) });
  }

  keys(o: Record<string, number>): string[] {
    return o ? Object.keys(o) : [];
  }
}
