import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pending = new BehaviorSubject<number>(0);
  readonly loading$ = this.pending.asObservable();

  show(): void { this.pending.next(this.pending.value + 1); }
  hide(): void { this.pending.next(Math.max(0, this.pending.value - 1)); }
}
