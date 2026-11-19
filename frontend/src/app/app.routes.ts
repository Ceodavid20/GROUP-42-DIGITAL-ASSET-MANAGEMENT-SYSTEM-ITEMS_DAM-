import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { AssetListComponent } from './features/assets/asset-list.component';
import { AssetDetailComponent } from './features/assets/asset-detail.component';
import { TransferListComponent } from './features/transfers/transfer-list.component';
import { MaintenanceListComponent } from './features/maintenance/maintenance-list.component';
import { ReportsComponent } from './features/reports/reports.component';
import { AuditComponent } from './features/audit/audit.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'assets', component: AssetListComponent, canActivate: [authGuard] },
  { path: 'assets/:id', component: AssetDetailComponent, canActivate: [authGuard] },
  { path: 'transfers', component: TransferListComponent, canActivate: [authGuard] },
  { path: 'maintenance', component: MaintenanceListComponent, canActivate: [authGuard] },
  { path: 'reports', component: ReportsComponent, canActivate: [authGuard] },
  {
    path: 'audit', component: AuditComponent, canActivate: [roleGuard('ROLE_ADMIN', 'ROLE_ASSET_OFFICER', 'ROLE_AUDITOR')],
    // navigation aid only — server still enforces (Section 3.2.4)
  },
  { path: '**', redirectTo: '' }
];
