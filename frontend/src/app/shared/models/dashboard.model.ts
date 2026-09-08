export interface DashboardSeriesItem {
  label: string;
  value: number | string;
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
  monthlyReceivedRepairs: DashboardSeriesItem[];
  averageTurnaroundDays: number;
  overdueOpenRepairs: number;
  inactiveDevices: DashboardInactiveDevice[];
}
