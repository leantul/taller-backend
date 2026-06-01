export type DeviceType = 'DESKTOP' | 'NOTEBOOK' | 'TABLET' | 'CELULAR' | 'OTROS';

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
  deviceType: DeviceType;
  clientId: string;
  currentPassword?: string;
  passwordHistory?: DevicePasswordHistory[];
  observations?: DeviceObservation[];
}
