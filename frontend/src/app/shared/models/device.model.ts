export interface Device {
  id?: string;
  brand: string;
  model: string;
  serialNumber: string;
  deviceType: 'DESKTOP' | 'NOTEBOOK' | 'TABLET' | 'CELULAR' | 'OTROS';
  clientId: string;
}
