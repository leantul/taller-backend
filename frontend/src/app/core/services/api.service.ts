import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private readonly http: HttpClient) {}

  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/client`);
  }

  createClient(payload: Client): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/client`, payload);
  }

  getDevices(): Observable<Device[]> {
    return this.http.get<Device[]>(`${this.baseUrl}/device`);
  }

  createDevice(payload: Device): Observable<Device> {
    return this.http.post<Device>(`${this.baseUrl}/device`, payload);
  }

  getRepairs(): Observable<Repair[]> {
    return this.http.get<Repair[]>(`${this.baseUrl}/repair`);
  }

  createRepair(payload: Repair): Observable<Repair> {
    return this.http.post<Repair>(`${this.baseUrl}/repair`, payload);
  }
}
