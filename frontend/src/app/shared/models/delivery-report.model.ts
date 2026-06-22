export interface RepairReportHardwareItem {
  id?: string;
  partName: string;
  quantity: number;
  detail?: string;
  unitPrice?: number;
  includePrice: boolean;
}

export interface RepairReportSoftwareItem {
  id?: string;
  softwareName: string;
  detail?: string;
}

export interface DeliveryReport {
  id?: string;
  repairId: string;
  orderNumber: string;
  issuedAt: string;
  clientName: string;
  clientLastName: string;
  clientPhone: string;
  clientEmail: string;
  clientDni: string;
  deviceTypeName: string;
  deviceBrand: string;
  deviceModel: string;
  deviceSerialNumber: string;
  reportedIssue: string;
  workPerformed: string;
  finalObservations?: string;
  showPartPrices: boolean;
  finalAmount?: number;
  hardwareItems: RepairReportHardwareItem[];
  softwareItems: RepairReportSoftwareItem[];
}

export interface SoftwareCatalogItem {
  id?: string;
  name: string;
  detail?: string;
}

export interface WorkshopSettings {
  id?: string;
  businessName: string;
  whatsapp?: string;
  instagram?: string;
  reportTitle: string;
  logoAssetPath: string;
}
