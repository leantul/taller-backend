import { DeviceObservation } from './device.model';

export interface RepairPart {
  id?: string;
  repairId?: string;
  name: string;
  quantity: number;
  provider?: string;
  cost: number;
  salePrice: number;
}

export interface RepairStatusHistory {
  id?: string;
  repairId?: string;
  status: Repair['status'];
  changedAt?: string;
}

export interface RepairPayment {
  id?: string;
  repairId?: string;
  amount: number;
  currency: 'ARS';
  paymentDate?: string;
  notes?: string;
}

export interface RepairStatusUpdate {
  status: Repair['status'];
  receiveDateTime?: string;
  returnDateTime?: string;
  paymentType?: 'FULL' | 'PARTIAL';
  paymentAmount?: number;
}

export interface Repair {
  id?: string;
  idDevice: string;
  idClient: string;
  orderNumber: string;
  description: string;
  status: 'POR_RECIBIR' | 'RECIBIDA' | 'PRESUPUESTADA_ESPERANDO_RESPUESTA' | 'HACIENDO' | 'ESPERANDO_RETIRO' | 'COBRADO_ESPERANDO_RETIRO' | 'RETIRADA_FALTA_COBRAR' | 'RETIRADA';
  price: number;
  laborAmount: number | null;
  quotedAmount?: number;
  quoteNotes?: string;
  repairNotes?: string;
  receiveDateTime?: string;
  returnDateTime?: string;
  parts?: RepairPart[];
  statusHistory?: RepairStatusHistory[];
  observations?: DeviceObservation[];
  payments?: RepairPayment[];
  totalPaid?: number;
  outstandingBalance?: number;
  paymentType?: 'FULL' | 'PARTIAL';
  paymentAmount?: number;
  clientName?: string;
  clientPhone?: string;
  deviceLabel?: string;
}

export interface StatusBoardRepair extends Repair {
  clientName: string;
  deviceLabel: string;
}


export type RepairCreateDTO = Omit<Repair, "id" | "returnDateTime"> & { orderNumber?: string };
export type RepairUpdateDTO = Partial<Repair> & Pick<Repair, "id">;
