import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { Client, ClientHistory, ClientListItem, PageResponse } from '../../shared/models/client.model';

@Injectable({ providedIn: 'root' })
export class ClientApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/client`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Client[]> { return this.http.get<Client[]>(this.url); }
  getById(id: string): Observable<Client> { return this.http.get<Client>(`${this.url}/${id}`); }
  getPage(page: number, size: number, term = ''): Observable<PageResponse<ClientListItem>> {
    return this.http.get<PageResponse<ClientListItem>>(`${this.url}/page?page=${page}&size=${size}&term=${encodeURIComponent(term)}`);
  }
  getHistory(id: string, page: number, size: number, includeClient: boolean): Observable<ClientHistory> {
    return this.http.get<ClientHistory>(`${this.url}/${id}/history?page=${page}&size=${size}&includeClient=${includeClient}`);
  }
  search(term: string): Observable<Client[]> { return this.http.get<Client[]>(`${this.url}/search?term=${encodeURIComponent(term)}`); }
  save(payload: Client): Observable<Client> { return this.http.post<Client>(this.url, payload); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
}
