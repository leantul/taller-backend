export interface Client {
  id?: string;
  name: string;
  lastName: string;
  dni: string;
  email: string;
  phone: string;
  address?: string;
  notes?: string;
  phones?: string[];
  emails?: string[];
  birthDate?: string;
}

export interface ClientListItem {
  id: string;
  name: string;
  lastName: string;
  email?: string;
  phone?: string;
  deviceCount: number;
}

export interface ClientRepairHistoryItem {
  id: string;
  orderNumber: string;
  status: import('./repair.model').Repair['status'];
  deviceBrand?: string;
  deviceModel?: string;
  receiveDateTime?: string;
  returnDateTime?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ClientHistory {
  client: Client | null;
  repairs: PageResponse<ClientRepairHistoryItem>;
}
