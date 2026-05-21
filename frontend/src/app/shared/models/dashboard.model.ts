import { Device } from './device.model';

export interface DashboardSeriesItem {
  label: string;
  value: number | string;
}

export interface DashboardRecentClient {
  id: string;
  name: string;
  deviceType: string;
}

export interface DashboardRecentDevice {
  id: string;
  deviceType: Device['deviceType'];
  brand: string;
  model: string;
}

export interface DashboardRecentRepair {
  repairId: string;
  date: string;
  client: string;
  price: number | string;
}

export interface DashboardInactiveDevice {
  name: string;
  lastRepair: string | null;
}

export interface DashboardOverview {
  clientCount: number;
  deviceCount: number;
  repairCount: number;
  waitingPickupCount: number;
  inProgressCount: number;
  quotedPendingCount: number;
  deviceTypes: DashboardSeriesItem[];
  repairStatuses: DashboardSeriesItem[];
  recentClients: DashboardRecentClient[];
  recentDevices: DashboardRecentDevice[];
  recentRepairs: DashboardRecentRepair[];
  inactiveDevices: DashboardInactiveDevice[];
}
