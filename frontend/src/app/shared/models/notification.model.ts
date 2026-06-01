import { DeviceType } from './device.model';
import { Repair, RepairPart } from './repair.model';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  readed: boolean;
  eventDate: string;
  type: 'WARRANTY_6_MONTHS' | 'WARRANTY_1_YEAR' | string;
  entityId: string;
  repairId?: string;
  deviceId?: string;
  clientId?: string;
  clientName?: string;
  clientLastName?: string;
  clientPhone?: string;
  clientEmail?: string;
  deviceType?: DeviceType;
  deviceBrand?: string;
  deviceModel?: string;
  deviceSerialNumber?: string;
  orderNumber?: string;
  repairDescription?: string;
  status?: Repair['status'];
  receiveDateTime?: string;
  returnDateTime?: string;
  quotedAmount?: number;
  price?: number;
  quoteNotes?: string;
  parts?: RepairPart[];
  observationId?: string;
  observationNote?: string;
  observationObservedAt?: string;
  observationFollowUpAt?: string;
}
