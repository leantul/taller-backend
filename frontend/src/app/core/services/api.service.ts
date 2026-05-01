import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../../shared/models/client.model';
import { APP_CONFIG } from '../config/app-config';
import { Device } from '../../shared/models/device.model';
import { Repair, RepairCreateDTO, RepairUpdateDTO } from '../../shared/models/repair.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = APP_CONFIG.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client`); }
  searchClients(term: string): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client/search?term=${encodeURIComponent(term)}`); }
  createClient(payload: Client): Observable<Client> { return this.http.post<Client>(`${this.baseUrl}/client`, payload); }
  deleteClient(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/client/${id}`); }

  getDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device`); }
  searchDevices(term: string): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device/search?term=${encodeURIComponent(term)}`); }
  createDevice(payload: Device): Observable<Device> { return this.http.post<Device>(`${this.baseUrl}/device`, payload); }
  updateDevice(payload: Device): Observable<Device> { return this.http.put<Device>(`${this.baseUrl}/device`, payload); }
  deleteDevice(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/device/${id}`); }

  getRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair`); }
  searchRepairs(term: string): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair/search?term=${encodeURIComponent(term)}`); }
  createRepair(payload: RepairCreateDTO): Observable<Repair> { return this.http.post<Repair>(`${this.baseUrl}/repair`, payload); }
  updateRepair(payload: RepairUpdateDTO): Observable<Repair> { return this.http.put<Repair>(`${this.baseUrl}/repair`, payload); }
  deleteRepair(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/repair/${id}`); }

  getLatestClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/dashboard/latest-clients`); }
  getLatestDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/dashboard/latest-devices`); }
  getLatestRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/dashboard/latest-repairs`); }
}
