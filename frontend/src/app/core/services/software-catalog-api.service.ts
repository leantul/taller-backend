import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { SoftwareCatalogItem } from '../../shared/models/delivery-report.model';

@Injectable({ providedIn: 'root' })
export class SoftwareCatalogApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/software-catalog`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<SoftwareCatalogItem[]> {
    return this.http.get<SoftwareCatalogItem[]>(this.url);
  }

  save(payload: SoftwareCatalogItem): Observable<SoftwareCatalogItem> {
    return this.http.post<SoftwareCatalogItem>(this.url, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
