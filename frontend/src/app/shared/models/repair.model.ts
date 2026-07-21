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

export interface Repair {
  id?: string;
  idDevice: string;
  idClient: string;
  orderNumber: string;
  description: string;
  status: 'POR_RECIBIR' | 'RECIBIDA' | 'PRESUPUESTADA_ESPERANDO_RESPUESTA' | 'HACIENDO' | 'ESPERANDO_RETIRO' | 'RETIRADA';
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
