export interface LoginResponse {
  token: string;
  username: string;
  name: string;
  roles: string[];
}

export type AssetStatus =
  'REGISTERED' | 'AVAILABLE' | 'ASSIGNED' |
  'UNDER_MAINTENANCE' | 'RETIRED' | 'DISPOSED';

export interface Asset {
  id: number;
  assetTag: string;
  name: string;
  brand?: string;
  model?: string;
  serialNumber?: string;
  macAddress?: string;
  status: AssetStatus;
  categoryId?: number;
  categoryName?: string;
  locationId?: number;
  locationUnit?: string;
  locationBuilding?: string;
  locationRoom?: string;
  custodianId?: number;
  custodianName?: string;
  custodianStaffId?: string;
  purchaseDate?: string;
  warrantyExpiry?: string;
  specifications?: string;
  notes?: string;
  qrCodeData?: string;
}

export interface Category { id: number; name: string; code?: string; description?: string; }
export interface UnitLocation {
  id: number; unitName: string; building: string; room?: string; campusZone?: string;
}

export type TransferDecision = 'PENDING' | 'APPROVED' | 'REJECTED';
export interface Transfer {
  id: number;
  asset?: { id: number; assetTag: string };
  fromLocation?: { unitName: string };
  toLocation?: { unitName: string };
  reason: string;
  decision: TransferDecision;
  decisionReason?: string;
  requestedAt: string;
  decidedAt?: string;
}

export type MaintenanceStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CANCELLED';
export interface MaintenanceCase {
  id: number;
  assetId: number;
  assetTag: string;
  reportedByName?: string;
  technicianName?: string;
  issueDescription: string;
  actionTaken?: string;
  status: MaintenanceStatus;
  openedOn: string;
  resolvedAt?: string;
  serviceCost?: number;
  sparePartsUsed?: string;
}

export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; }
