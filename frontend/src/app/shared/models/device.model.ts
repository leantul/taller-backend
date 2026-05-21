export interface DevicePasswordHistory {
  id?: string;
  value: string;
  isCurrent: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Device {
  id?: string;
  brand: string;
  model: string;
  serialNumber: string;
  deviceType: 'DESKTOP' | 'NOTEBOOK' | 'TABLET' | 'CELULAR' | 'OTROS';
  clientId: string;
  currentPassword?: string;
  passwordHistory?: DevicePasswordHistory[];
}
