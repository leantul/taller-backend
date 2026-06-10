import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { DashboardOverview } from '../../shared/models/dashboard.model';
import { FinanceSummary } from '../../shared/models/finance.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

@Injectable({ providedIn: 'root' })
export class ReportingApiService {
  constructor(private readonly http: HttpClient) {}

  getDashboardOverview(): Observable<DashboardOverview> { return this.http.get<DashboardOverview>(`${APP_CONFIG.apiUrl}/dashboard/overview`); }
  getLatestClients(): Observable<Client[]> { return this.http.get<Client[]>(`${APP_CONFIG.apiUrl}/dashboard/latest-clients`); }
  getLatestDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${APP_CONFIG.apiUrl}/dashboard/latest-devices`); }
  getLatestRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${APP_CONFIG.apiUrl}/dashboard/latest-repairs`); }
  getFinanceSummary(from?: string, to?: string): Observable<FinanceSummary> {
    const params = new URLSearchParams();
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    const query = params.toString();
    return this.http.get<FinanceSummary>(`${APP_CONFIG.apiUrl}/finance/summary${query ? `?${query}` : ''}`);
  }
}
