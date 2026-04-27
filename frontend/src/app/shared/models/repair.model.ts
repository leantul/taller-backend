export interface Repair {
  id?: string;
  idDevice: string;
  idClient: string;
  orderNumber: string;
  description: string;
  status: 'POR_RECIBIR' | 'RECIBIDA' | 'PRESUPUESTADA_ESPERANDO_RESPUESTA' | 'HACIENDO' | 'ESPERANDO_RETIRO' | 'RETIRADA';
  price: number;
}
