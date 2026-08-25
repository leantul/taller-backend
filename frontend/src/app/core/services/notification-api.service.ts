import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { NotificationItem } from '../../shared/models/notification.model';
import { PageResponse } from '../../shared/models/client.model';

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/notifications`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<NotificationItem[]> { return this.http.get<NotificationItem[]>(this.url); }
  getPage(page = 0, size = 20): Observable<PageResponse<NotificationItem>> { return this.http.get<PageResponse<NotificationItem>>(`${this.url}/page?page=${page}&size=${size}`); }
  getUnreadCount(): Observable<number> { return this.http.get<number>(`${this.url}/unread-count`); }
  markAsRead(id: string): Observable<void> { return this.http.patch<void>(`${this.url}/${id}/read`, {}); }
}
