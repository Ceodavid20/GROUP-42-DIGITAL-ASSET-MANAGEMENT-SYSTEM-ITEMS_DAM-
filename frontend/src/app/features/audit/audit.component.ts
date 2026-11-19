import { Component, OnInit, signal } from '@angular/core';
import { NgFor, NgIf, SlicePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [NgFor, NgIf, SlicePipe],
  template: `
  <h1>Audit trail</h1>
  <p class="lede">Append-oriented, read-only. Actor / action / time / entity for every key change (FR-09).</p>
  <div class="ui-card pad">
    <div class="row" *ngFor="let e of events()">
      <div><strong>{{ e.action }}</strong> — {{ e.entityType }} #{{ e.entityId }}
        <div class="meta">{{ e.details }}</div></div>
      <div class="meta">{{ e.actorName ?? 'system' }} ({{ e.actorRole ?? '—' }})<br>{{ e.timestamp | slice:0:19 }}</div>
    </div>
    <div class="empty" *ngIf="events().length === 0">No audit events yet.</div>
  </div>`,
  styles: [`
    .lede { color:var(--text-secondary); margin:4px 0 14px; }
    .pad { padding:18px; }
    .row { display:flex; justify-content:space-between; gap:10px; padding:10px 0;
      border-top:1px solid var(--border-color); font-size:0.86rem; }
    .meta { color:var(--text-secondary); font-size:0.78rem; text-align:right; }
    .row .meta:first-child { text-align:left; }
    .empty { color:var(--text-muted); }
  `]
})
export class AuditComponent implements OnInit {
  events = signal<any[]>([]);

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.get<any[]>('/audit-events/recent').subscribe({ next: (e) => this.events.set(e) });
  }
}
