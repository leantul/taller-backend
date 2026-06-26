import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { Device, DeviceObservation, DeviceType } from '../../shared/models/device.model';
import { PageResponse } from '../../shared/models/client.model';

@Injectable({ providedIn: 'root' })
export class DeviceApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/device`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Device[]> { return this.http.get<Device[]>(this.url); }
  getPage(page: number, size: number, term = '', clientId = '', clientTerm = '', sortBy = 'createdAt', sortDir: 'asc' | 'desc' = 'desc'): Observable<PageResponse<Device>> {
    const params = new URLSearchParams({ page: String(page), size: String(size), term, clientId, clientTerm, sortBy, sortDir });
    return this.http.get<PageResponse<Device>>(`${this.url}/page?${params.toString()}`);
  }
  getTypes(): Observable<DeviceType[]> { return this.http.get<DeviceType[]>(`${APP_CONFIG.apiUrl}/device-type`); }
  getById(id: string): Observable<Device> { return this.http.get<Device>(`${this.url}/${id}`); }
  search(term: string): Observable<Device[]> { return this.http.get<Device[]>(`${this.url}/search?term=${encodeURIComponent(term)}`); }
  create(payload: Device): Observable<Device> { return this.http.post<Device>(this.url, payload); }
  update(payload: Device): Observable<Device> { return this.http.put<Device>(this.url, payload); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
  addPassword(deviceId: string, value: string): Observable<Device> { return this.http.post<Device>(`${this.url}/${deviceId}/passwords`, { value }); }
  updatePassword(deviceId: string, passwordId: string, value: string): Observable<Device> { return this.http.put<Device>(`${this.url}/${deviceId}/passwords/${passwordId}`, { value }); }
  deletePassword(deviceId: string, passwordId: string): Observable<Device> { return this.http.delete<Device>(`${this.url}/${deviceId}/passwords/${passwordId}`); }
  makeCurrentPassword(deviceId: string, passwordId: string): Observable<Device> { return this.http.post<Device>(`${this.url}/${deviceId}/passwords/${passwordId}/make-current`, {}); }
  addObservation(deviceId: string, payload: DeviceObservation): Observable<Device> { return this.http.post<Device>(`${this.url}/${deviceId}/observations`, payload); }
  updateObservation(deviceId: string, observationId: string, payload: DeviceObservation): Observable<Device> { return this.http.put<Device>(`${this.url}/${deviceId}/observations/${observationId}`, payload); }
  resolveObservation(deviceId: string, observationId: string): Observable<Device> { return this.http.patch<Device>(`${this.url}/${deviceId}/observations/${observationId}/resolve`, {}); }
  deleteObservation(deviceId: string, observationId: string): Observable<Device> { return this.http.delete<Device>(`${this.url}/${deviceId}/observations/${observationId}`); }
}
