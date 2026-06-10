import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { Repair, RepairCreateDTO, RepairUpdateDTO, StatusBoardRepair } from '../../shared/models/repair.model';

@Injectable({ providedIn: 'root' })
export class RepairApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/repair`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Repair[]> { return this.http.get<Repair[]>(this.url); }
  getStatusBoard(): Observable<StatusBoardRepair[]> { return this.http.get<StatusBoardRepair[]>(`${this.url}/status-board`); }
  getById(id: string): Observable<Repair> { return this.http.get<Repair>(`${this.url}/${id}`); }
  search(term: string): Observable<Repair[]> { return this.http.get<Repair[]>(`${this.url}/search?term=${encodeURIComponent(term)}`); }
  create(payload: RepairCreateDTO): Observable<Repair> { return this.http.post<Repair>(this.url, payload); }
  update(payload: RepairUpdateDTO): Observable<Repair> { return this.http.put<Repair>(this.url, payload); }
  updateStatus(id: string, status: Repair['status']): Observable<void> { return this.http.patch<void>(`${this.url}/${id}/status`, { status }); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
}
