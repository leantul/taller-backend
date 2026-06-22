import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { WorkshopSettings } from '../../shared/models/delivery-report.model';

@Injectable({ providedIn: 'root' })
export class WorkshopSettingsApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/workshop-settings`;

  constructor(private readonly http: HttpClient) {}

  get(): Observable<WorkshopSettings> {
    return this.http.get<WorkshopSettings>(this.url);
  }

  update(payload: WorkshopSettings): Observable<WorkshopSettings> {
    return this.http.put<WorkshopSettings>(this.url, payload);
  }
}
