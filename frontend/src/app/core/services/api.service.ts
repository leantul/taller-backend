import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../../shared/models/client.model';
import { APP_CONFIG } from '../config/app-config';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = APP_CONFIG.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client`); }
  searchClients(term: string): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/client/search?term=${encodeURIComponent(term)}`); }
  createClient(payload: Client): Observable<Client> { return this.http.post<Client>(`${this.baseUrl}/client`, payload); }

  getDevices(): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device`); }
  searchDevices(term: string): Observable<Device[]> { return this.http.get<Device[]>(`${this.baseUrl}/device/search?term=${encodeURIComponent(term)}`); }
  createDevice(payload: Device): Observable<Device> { return this.http.post<Device>(`${this.baseUrl}/device`, payload); }

  getRepairs(): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair`); }
  searchRepairs(term: string): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.baseUrl}/repair/search?term=${encodeURIComponent(term)}`); }
  createRepair(payload: Repair): Observable<Repair> { return this.http.post<Repair>(`${this.baseUrl}/repair`, payload); }
  updateRepair(payload: Repair): Observable<Repair> { return this.http.put<Repair>(`${this.baseUrl}/repair`, payload); }
}
