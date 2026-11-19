import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, SlicePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Page, Transfer } from '../../core/models/models';

@Component({
  selector: 'app-transfer-list',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, SlicePipe],
  template: `
  <h1>Controlled transfers</h1>
  <p class="lede">Request recorded separately; only an approved decision moves the location (Fig. 3.4). Repeat clicks are idempotent.</p>
  <div class="ui-card pad">
    <h2>Pending approval</h2>
    <div class="row" *ngFor="let t of pending()?.content">
      <div>
        <strong>{{ t.asset?.assetTag }}</strong> — {{ t.fromLocation?.unitName }} &rarr; {{ t.toLocation?.unitName }}
        <div class="reason">{{ t.reason }} &middot; {{ t.requestedAt | slice:0:10 }}</div>
      </div>
      <div class="btnrow" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER')">
        <input [(ngModel)]="reasons[t.id]" placeholder="Decision note">
        <button class="btn btn-primary btn-sm" (click)="decide(t.id, 'APPROVED')">Approve</button>
        <button class="btn btn-secondary btn-sm" (click)="decide(t.id, 'REJECTED')">Reject</button>
      </div>
    </div>
    <div class="empty" *ngIf="pending() && pending()!.content.length === 0">No pending transfers.</div>
  </div>
  <div class="err" *ngIf="error()">{{ error() }}</div>`,
  styles: [`
    .lede { color:var(--text-secondary); margin:4px 0 14px; }
    .pad { padding:18px; } h2 { font-size:1rem; margin-bottom:8px; }
    .row { display:flex; justify-content:space-between; gap:12px; align-items:center;
      padding:10px 0; border-top:1px solid var(--border-color); font-size:0.88rem; flex-wrap:wrap; }
    .reason { color:var(--text-secondary); font-size:0.8rem; }
    .btnrow { display:flex; gap:6px; align-items:center; }
    .btnrow input { padding:7px 9px; border:1px solid var(--border-color); border-radius:6px; }
    .empty { color:var(--text-muted); padding:10px 0; }
    .err { color:var(--ui-crimson); margin-top:10px; }
  `]
})
export class TransferListComponent implements OnInit {
  pending = signal<Page<Transfer> | null>(null);
  reasons: Record<number, string> = {};
  error = signal<string | null>(null);

  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.get<Page<Transfer>>('/transfers/pending', { size: 50 })
      .subscribe({ next: (p) => this.pending.set(p) });
  }

  decide(id: number, decision: string): void {
    this.error.set(null);
    this.api.post(`/transfers/${id}/decision`, { decision, decisionReason: this.reasons[id] ?? '' })
      .subscribe({ next: () => this.load(), error: (e) => this.error.set(e.error?.message ?? 'Decision failed.') });
  }
}
