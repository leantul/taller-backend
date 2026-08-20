export interface DeviceType {
  id: string;
  name: string;
}

export interface DevicePasswordHistory {
  id?: string;
  value: string;
  isCurrent: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DeviceObservation {
  id?: string;
  deviceId?: string;
  repairId?: string;
  note: string;
  observedAt?: string;
  followUpAt?: string;
  resolvedAt?: string;
}

export interface Device {
  id?: string;
  brand: string;
  model: string;
  serialNumber: string;
  deviceTypeId: string;
  deviceTypeName?: string;
  clientId: string;
  technicalDetails?: string;
  currentPassword?: string;
  clientName?: string;
  passwordHistory?: DevicePasswordHistory[];
  observations?: DeviceObservation[];
}

export interface DeviceRepairHistoryItem {
  id: string;
  orderNumber: string;
  status: import('./repair.model').Repair['status'];
  description?: string;
  receiveDateTime?: string;
  returnDateTime?: string;
}
