import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface ErrorDialogState {
  title: string;
  detail: string;
}

@Injectable({ providedIn: 'root' })
export class ErrorDialogService {
  private readonly stateSubject = new BehaviorSubject<ErrorDialogState | null>(null);

  readonly state$ = this.stateSubject.asObservable();

  show(title: string, detail: string): void {
    this.stateSubject.next({
      title: title.trim() || 'Error',
      detail: detail.trim() || 'Ocurrió un error inesperado.'
    });
  }

  close(): void {
    this.stateSubject.next(null);
  }
}
