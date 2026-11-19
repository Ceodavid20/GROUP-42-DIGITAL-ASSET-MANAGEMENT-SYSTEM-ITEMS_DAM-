import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, SlicePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Asset, MaintenanceCase, UnitLocation } from '../../core/models/models';

@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, SlicePipe, RouterLink],
  template: `
  <a routerLink="/assets">&larr; Back to catalogue</a>
  <div class="ui-card pad" *ngIf="asset() as a">
    <div class="head">
      <div>
        <h1>{{ a.assetTag }}</h1>
        <p class="sub">{{ a.name }} &middot; QR resolves to this record (FR-11): <code>{{ a.qrCodeData }}</code></p>
      </div>
      <span class="badge badge-type">{{ a.status }}</span>
    </div>
    <div class="facts">
      <div><span>Category</span><strong>{{ a.categoryName }}</strong></div>
      <div><span>Location</span><strong>{{ a.locationUnit }} — {{ a.locationBuilding }} {{ a.locationRoom }}</strong></div>
      <div><span>Custodian</span><strong>{{ a.custodianName ?? '—' }} {{ a.custodianStaffId ? '(' + a.custodianStaffId + ')' : '' }}</strong></div>
      <div><span>Serial</span><strong>{{ a.serialNumber ?? '—' }}</strong></div>
      <div><span>Brand / Model</span><strong>{{ a.brand ?? '—' }} {{ a.model ?? '' }}</strong></div>
      <div><span>Warranty</span><strong>{{ a.warrantyExpiry ?? '—' }}</strong></div>
    </div>
    <p class="notes" *ngIf="a.notes">{{ a.notes }}</p>

    <div class="ops">
      <div class="ui-card inner" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER') && a.status === 'REGISTERED'">
        <h3>Verify intake</h3>
        <button class="btn btn-primary btn-sm" (click)="verify(a.id)">Mark AVAILABLE</button>
      </div>
      <div class="ui-card inner" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER') && a.status === 'AVAILABLE'">
        <h3>Issue equipment (FR-04)</h3>
        <input [(ngModel)]="custodianId" type="number" placeholder="Custodian user ID">
        <input [(ngModel)]="condition" placeholder="Condition on issue">
        <button class="btn btn-primary btn-sm" (click)="issue(a.id)">Issue (ASSIGNED)</button>
      </div>
      <div class="ui-card inner" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER','ROLE_UNIT_CUSTODIAN')">
        <h3>Request transfer (FR-05)</h3>
        <select [(ngModel)]="toLocation"><option [ngValue]="null">Destination…</option>
          <option *ngFor="let l of locations()" [ngValue]="l.id">{{ l.unitName }} — {{ l.building }}</option></select>
        <input [(ngModel)]="reason" placeholder="Justification reason">
        <button class="btn btn-secondary btn-sm" (click)="transfer(a.id)">Submit request</button>
      </div>
      <div class="ui-card inner" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER','ROLE_UNIT_CUSTODIAN')">
        <h3>Open maintenance (FR-06)</h3>
        <input [(ngModel)]="faultDesc" placeholder="Defect description">
        <button class="btn btn-secondary btn-sm" (click)="openMaint(a.id)">Open ticket</button>
      </div>
      <div class="ui-card inner danger" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER')">
        <h3>Retire / dispose (FR-07)</h3>
        <input [(ngModel)]="retireReason" placeholder="Approval reason / sanitization evidence">
        <div class="btnrow">
          <button class="btn btn-secondary btn-sm" (click)="retire(a.id)">Retire</button>
          <button class="btn btn-secondary btn-sm" (click)="dispose(a.id)">Dispose</button>
        </div>
      </div>
    </div>
    <div class="err" *ngIf="error()">{{ error() }}</div>
    <div class="ok" *ngIf="ok()">{{ ok() }}</div>

    <h2>Maintenance history</h2>
    <div class="row" *ngFor="let m of history()">
      <span>{{ m.openedOn | slice:0:10 }} — {{ m.issueDescription }}</span>
      <span class="badge badge-type">{{ m.status }}</span>
    </div>

    <h2 *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER','ROLE_AUDITOR')">Restricted evidence (attachments)</h2>
    <div class="row" *ngFor="let a of attachments()">
      <span><strong>{{ a.fileName }}</strong> — {{ a.accessLabel }} ({{ a.mimeType }})</span>
      <a [href]="'/api/v1/attachments/' + a.id + '/download'">Download</a>
    </div>
    <div class="attach" *ngIf="auth.hasRole('ROLE_ADMIN','ROLE_ASSET_OFFICER')">
      <input type="file" (change)="onFile($event)">
      <input [(ngModel)]="accessLabel" placeholder="Access label e.g. WARRANTY_DOC">
      <button class="btn btn-secondary btn-sm" (click)="upload()">Upload evidence</button>
    </div>
  </div>`,
  styles: [`
    .pad { padding:20px; margin-top:10px; }
    .head { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; }
    .sub { color:var(--text-secondary); font-size:0.88rem; }
    .facts { display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:8px; margin:14px 0; }
    .facts span { display:block; font-size:0.72rem; color:var(--text-muted); text-transform:uppercase; }
    .notes { background:#F8FAFC; border:1px solid var(--border-color); border-radius:8px; padding:10px 12px; font-size:0.86rem; white-space:pre-line; }
    .ops { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:10px; margin:14px 0; }
    .inner { padding:14px; } .inner h3 { font-size:0.9rem; margin-bottom:8px; }
    .inner input, .inner select { width:100%; margin-bottom:8px; padding:8px 10px; border:1px solid var(--border-color); border-radius:6px; }
    .danger { border-color:#FECACA; } .btnrow { display:flex; gap:8px; }
    .err { color:var(--ui-crimson); } .ok { color:var(--ui-emerald); }
    h2 { font-size:1rem; margin:12px 0 6px; }
    .row { display:flex; justify-content:space-between; padding:8px 0; border-top:1px solid var(--border-color); font-size:0.86rem; }
  `]
})
export class AssetDetailComponent implements OnInit {
  asset = signal<Asset | null>(null);
  history = signal<MaintenanceCase[]>([]);
  attachments = signal<any[]>([]);
  locations = signal<UnitLocation[]>([]);
  error = signal<string | null>(null);
  ok = signal<string | null>(null);
  private file: File | null = null;
  accessLabel = 'WARRANTY_DOC';
  custodianId!: number;
  condition = '';
  toLocation: number | null = null;
  reason = '';
  faultDesc = '';
  retireReason = '';
  private id!: number;

  constructor(private route: ActivatedRoute, private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.reload();
    this.api.get<UnitLocation[]>('/reference/locations').subscribe({ next: (l) => this.locations.set(l) });
  }

  reload(): void {
    this.api.get<Asset>(`/assets/${this.id}`).subscribe({ next: (a) => this.asset.set(a) });
    this.api.get<MaintenanceCase[]>(`/maintenance/asset/${this.id}`).subscribe({ next: (h) => this.history.set(h) });
    if (this.auth.hasRole('ROLE_ADMIN', 'ROLE_ASSET_OFFICER', 'ROLE_AUDITOR')) {
      this.api.get<any[]>(`/attachments/asset/${this.id}`).subscribe({ next: (a) => this.attachments.set(a) });
    }
  }

  onFile(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.file = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  upload(): void {
    if (!this.file) { this.fail({ error: { message: 'Choose a file first.' } }, 'Choose a file first.'); return; }
    const fd = new FormData();
    fd.append('assetId', String(this.id));
    fd.append('file', this.file);
    fd.append('accessLabel', this.accessLabel);
    // FormData bypasses JSON ApiService; use fetch with the stored token
    const token = localStorage.getItem('dam_token');
    fetch('/api/v1/attachments', {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: fd
    }).then(async (r) => {
      if (!r.ok) throw new Error((await r.json()).message ?? 'Upload failed');
      this.file = null;
      this.done('Evidence uploaded.');
    }).catch((e) => this.fail({ error: { message: e.message } }, 'Upload failed.'));
  }

  private done(msg: string): void {
    this.error.set(null);
    this.ok.set(msg);
    this.reload();
  }

  private fail(e: any, fallback: string): void {
    this.ok.set(null);
    this.error.set(e.error?.message ?? fallback);
  }

  verify(id: number): void {
    this.api.post<Asset>(`/assets/${id}/verify`, {}).subscribe({ next: () => this.done('Verified — now AVAILABLE.'), error: (e) => this.fail(e, 'Verify failed.') });
  }

  issue(id: number): void {
    this.api.post(`/assignments`, { assetId: id, custodianId: this.custodianId, conditionOnIssue: this.condition })
      .subscribe({ next: () => this.done('Issued — single assignment, state ASSIGNED (TC-03).'), error: (e) => this.fail(e, 'Issue rejected — already assigned or invalid (TC-04).') });
  }

  transfer(id: number): void {
    this.api.post(`/transfers`, { assetId: id, toLocationId: this.toLocation, reason: this.reason })
      .subscribe({ next: () => this.done('Transfer request recorded — location changes only on approval (TC-05/06).'), error: (e) => this.fail(e, 'Transfer request failed.') });
  }

  openMaint(id: number): void {
    this.api.post(`/maintenance`, { assetId: id, issueDescription: this.faultDesc })
      .subscribe({ next: () => this.done('Maintenance case opened (TC-07).'), error: (e) => this.fail(e, 'Could not open maintenance.') });
  }

  retire(id: number): void {
    this.api.post<Asset>(`/assets/${id}/retire`, { reason: this.retireReason })
      .subscribe({ next: () => this.done('Retired with approval reason recorded.'), error: (e) => this.fail(e, 'Retire failed.') });
  }

  dispose(id: number): void {
    this.api.post<Asset>(`/assets/${id}/dispose`, { evidence: this.retireReason })
      .subscribe({ next: () => this.done('Disposed with evidence recorded.'), error: (e) => this.fail(e, 'Dispose failed — must be RETIRED first.') });
  }
}
