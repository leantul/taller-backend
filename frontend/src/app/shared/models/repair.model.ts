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

export interface Repair {
  id?: string;
  idDevice: string;
  idClient: string;
  orderNumber: string;
  description: string;
  status: 'POR_RECIBIR' | 'RECIBIDA' | 'PRESUPUESTADA_ESPERANDO_RESPUESTA' | 'HACIENDO' | 'ESPERANDO_RETIRO' | 'RETIRADA';
  price: number;
  laborAmount?: number;
  quotedAmount?: number;
  quoteNotes?: string;
  receiveDateTime?: string;
  returnDateTime?: string;
  parts?: RepairPart[];
  observations?: DeviceObservation[];
}


export type RepairCreateDTO = Omit<Repair, "id" | "receiveDateTime" | "returnDateTime"> & { orderNumber?: string };
export type RepairUpdateDTO = Partial<Repair> & Pick<Repair, "id">;
