import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { DeliveryReport } from '../../shared/models/delivery-report.model';

@Injectable({ providedIn: 'root' })
export class DeliveryReportApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/repair`;

  constructor(private readonly http: HttpClient) {}

  getByRepairId(repairId: string): Observable<DeliveryReport> {
    return this.http.get<DeliveryReport>(`${this.url}/${repairId}/delivery-report`);
  }

  save(repairId: string, payload: DeliveryReport): Observable<DeliveryReport> {
    return this.http.put<DeliveryReport>(`${this.url}/${repairId}/delivery-report`, payload);
  }

  getPdf(repairId: string): Observable<Blob> {
    return this.http.get(`${this.url}/${repairId}/delivery-report/pdf`, { responseType: 'blob' });
  }
}
