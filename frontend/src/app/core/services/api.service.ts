import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../../shared/models/client.model';
import { APP_CONFIG } from '../config/app-config';
import { Device, DeviceObservation, DevicePasswordHistory, DeviceType } from '../../shared/models/device.model';
import { Repair, RepairCreateDTO, RepairUpdateDTO } from '../../shared/models/repair.model';
import { FinanceSummary } from '../../shared/models/finance.model';
import { DashboardOverview } from '../../shared/models/dashboard.model';
import { NotificationItem } from '../../shared/models/notification.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = APP_CONFIG.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client`); }
  searchClients(term: string): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client/search?term=${encodeURIComponent(term)}`); }
  createClient(payload: Client): Observable<Client> { return this.http.post<Client>(`${this.baseUrl}/client`, payload); }
  deleteClient(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/client/${id}`); }

  getDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device`); }
  getDeviceTypes(): Observable<DeviceType[]> { return this.http.get<DeviceType[]>(`${this.baseUrl}/device-type`); }
  getDeviceById(id: string): Observable<Device> { return this.http.get<Device>(`${this.baseUrl}/device/${id}`); }
  searchDevices(term: string): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device/search?term=${encodeURIComponent(term)}`); }
  createDevice(payload: Device): Observable<Device> { return this.http.post<Device>(`${this.baseUrl}/device`, payload); }
  updateDevice(payload: Device): Observable<Device> { return this.http.put<Device>(`${this.baseUrl}/device`, payload); }
  deleteDevice(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/device/${id}`); }
  addDevicePassword(deviceId: string, value: string): Observable<Device> {
    return this.http.post<Device>(`${this.baseUrl}/device/${deviceId}/passwords`, { value });
  }
  updateDevicePassword(deviceId: string, passwordId: string, value: string): Observable<Device> {
    return this.http.put<Device>(`${this.baseUrl}/device/${deviceId}/passwords/${passwordId}`, { value });
  }
  deleteDevicePassword(deviceId: string, passwordId: string): Observable<Device> {
    return this.http.delete<Device>(`${this.baseUrl}/device/${deviceId}/passwords/${passwordId}`);
  }
  makeCurrentDevicePassword(deviceId: string, passwordId: string): Observable<Device> {
    return this.http.post<Device>(`${this.baseUrl}/device/${deviceId}/passwords/${passwordId}/make-current`, {});
  }
  addDeviceObservation(deviceId: string, payload: DeviceObservation): Observable<Device> {
    return this.http.post<Device>(`${this.baseUrl}/device/${deviceId}/observations`, payload);
  }
  updateDeviceObservation(deviceId: string, observationId: string, payload: DeviceObservation): Observable<Device> {
    return this.http.put<Device>(`${this.baseUrl}/device/${deviceId}/observations/${observationId}`, payload);
  }
  resolveDeviceObservation(deviceId: string, observationId: string): Observable<Device> {
    return this.http.patch<Device>(`${this.baseUrl}/device/${deviceId}/observations/${observationId}/resolve`, {});
  }
  deleteDeviceObservation(deviceId: string, observationId: string): Observable<Device> {
    return this.http.delete<Device>(`${this.baseUrl}/device/${deviceId}/observations/${observationId}`);
  }

  getRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair`); }
  getRepairById(id: string): Observable<Repair> { return this.http.get<Repair>(`${this.baseUrl}/repair/${id}`); }
  searchRepairs(term: string): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair/search?term=${encodeURIComponent(term)}`); }
  createRepair(payload: RepairCreateDTO): Observable<Repair> { return this.http.post<Repair>(`${this.baseUrl}/repair`, payload); }
  updateRepair(payload: RepairUpdateDTO): Observable<Repair> { return this.http.put<Repair>(`${this.baseUrl}/repair`, payload); }
  deleteRepair(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/repair/${id}`); }
  getNotifications(): Observable<NotificationItem[]> { return this.http.get<NotificationItem[]>(`${this.baseUrl}/notifications`); }
  getUnreadNotificationCount(): Observable<number> { return this.http.get<number>(`${this.baseUrl}/notifications/unread-count`); }
  markNotificationAsRead(id: string): Observable<void> { return this.http.patch<void>(`${this.baseUrl}/notifications/${id}/read`, {}); }

  getDashboardOverview(): Observable<DashboardOverview> { return this.http.get<DashboardOverview>(`${this.baseUrl}/dashboard/overview`); }
  getLatestClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/dashboard/latest-clients`); }
  getLatestDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/dashboard/latest-devices`); }
  getLatestRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/dashboard/latest-repairs`); }
  getFinanceSummary(from?: string, to?: string): Observable<FinanceSummary> {
    const params = new URLSearchParams();
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    const query = params.toString();
    return this.http.get<FinanceSummary>(`${this.baseUrl}/finance/summary${query ? `?${query}` : ''}`);
  }
}
