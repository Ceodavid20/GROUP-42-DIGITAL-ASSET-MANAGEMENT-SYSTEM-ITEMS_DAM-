import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, SlicePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { MaintenanceCase, Page } from '../../core/models/models';

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, SlicePipe],
  template: `
  <h1>Maintenance</h1>
  <div class="ui-card filters">
    <select [(ngModel)]="status" (change)="load()">
      <option>OPEN</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>CANCELLED</option>
    </select>
    <button class="btn btn-primary btn-sm" (click)="load()">Refresh</button>
  </div>
  <div class="ui-card pad">
    <div class="row" *ngFor="let m of page()?.content">
      <div>
        <strong>{{ m.assetTag }}</strong> — {{ m.issueDescription }}
        <div class="meta">Opened {{ m.openedOn | slice:0:10 }} &middot; Tech: {{ m.technicianName ?? 'unassigned' }}
          <span *ngIf="m.actionTaken"> &middot; {{ m.actionTaken }}</span></div>
      </div>
      <div class="btnrow">
        <span class="badge badge-type">{{ m.status }}</span>
        <button class="btn btn-secondary btn-sm" (click)="advance(m, 'IN_PROGRESS')" *ngIf="m.status === 'OPEN'">Start</button>
        <button class="btn btn-secondary btn-sm" (click)="advance(m, 'RESOLVED')" *ngIf="m.status !== 'RESOLVED' && m.status !== 'CANCELLED'">Resolve</button>
      </div>
    </div>
    <div class="empty" *ngIf="page() && page()!.content.length === 0">No {{ status }} cases.</div>
  </div>`,
  styles: [`
    .filters { display:flex; gap:8px; padding:14px; margin:12px 0; }
    .filters select { padding:9px 10px; border:1px solid var(--border-color); border-radius:var(--radius-sm); }
    .pad { padding:18px; }
    .row { display:flex; justify-content:space-between; gap:12px; align-items:center;
      padding:10px 0; border-top:1px solid var(--border-color); font-size:0.88rem; flex-wrap:wrap; }
    .meta { color:var(--text-secondary); font-size:0.8rem; }
    .btnrow { display:flex; gap:6px; align-items:center; }
    .empty { color:var(--text-muted); }
  `]
})
export class MaintenanceListComponent implements OnInit {
  status = 'OPEN';
  page = signal<Page<MaintenanceCase> | null>(null);

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.get<Page<MaintenanceCase>>('/maintenance', { status: this.status, size: 50 })
      .subscribe({ next: (p) => this.page.set(p) });
  }

  advance(m: MaintenanceCase, status: string): void {
    const actionTaken = status === 'RESOLVED' ? prompt('Service action taken:', m.actionTaken ?? '') ?? undefined : undefined;
    this.api.patch(`/maintenance/${m.id}`, { status, actionTaken })
      .subscribe({ next: () => this.load() });
  }
}
