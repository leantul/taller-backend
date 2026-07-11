import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject, catchError, exhaustMap, of } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class NotificationStateService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  private readonly refreshSubject = new Subject<void>();
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private readonly api: ApiService) {
    this.refreshSubject.pipe(
      exhaustMap(() => this.api.getUnreadNotificationCount().pipe(
        catchError(() => of(0))
      ))
    ).subscribe({
      next: (count) => this.unreadCountSubject.next(count)
    });
  }

  refreshUnreadCount(): void {
    this.refreshSubject.next();
  }

  decrementUnreadCount(): void {
    this.unreadCountSubject.next(Math.max(0, this.unreadCountSubject.value - 1));
  }
}
