import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { DashboardOverview } from '../../shared/models/dashboard.model';
import { FinancePage, FinanceSummary } from '../../shared/models/finance.model';

@Injectable({ providedIn: 'root' })
export class ReportingApiService {
  constructor(private readonly http: HttpClient) {}

  getDashboardOverview(): Observable<DashboardOverview> { return this.http.get<DashboardOverview>(`${APP_CONFIG.apiUrl}/dashboard/overview`); }
  getFinanceSummary(from?: string, to?: string): Observable<FinanceSummary> {
    const params = new URLSearchParams();
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    const query = params.toString();
    return this.http.get<FinanceSummary>(`${APP_CONFIG.apiUrl}/finance/summary${query ? `?${query}` : ''}`);
  }

  getFinanceDetails(
    from: string | undefined,
    to: string | undefined,
    page: number,
    size: number,
    sortBy: string,
    sortDir: 'asc' | 'desc'
  ): Observable<FinancePage> {
    const params = new URLSearchParams({
      page: `${page}`,
      size: `${size}`,
      sortBy,
      sortDir
    });
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    return this.http.get<FinancePage>(`${APP_CONFIG.apiUrl}/finance/details?${params.toString()}`);
  }
}
