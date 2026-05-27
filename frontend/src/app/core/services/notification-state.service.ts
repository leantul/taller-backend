import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class NotificationStateService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private readonly api: ApiService) {}

  refreshUnreadCount(): void {
    this.api.getUnreadNotificationCount().subscribe({
      next: (count) => this.unreadCountSubject.next(count),
      error: () => this.unreadCountSubject.next(0)
    });
  }

  decrementUnreadCount(): void {
    this.unreadCountSubject.next(Math.max(0, this.unreadCountSubject.value - 1));
  }
}
