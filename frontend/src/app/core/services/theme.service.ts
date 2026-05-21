import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly key = 'theme-mode';
  private readonly modeSubject = new BehaviorSubject<ThemeMode>('dark');

  readonly mode$ = this.modeSubject.asObservable();

  initTheme(): ThemeMode {
    const stored = localStorage.getItem(this.key) as ThemeMode | null;
    const mode = stored ?? 'dark';
    this.setTheme(mode);
    return mode;
  }

  toggleTheme(current: ThemeMode): ThemeMode {
    const next: ThemeMode = current === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
    return next;
  }

  currentTheme(): ThemeMode {
    return this.modeSubject.value;
  }

  private setTheme(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
    localStorage.setItem(this.key, mode);
    this.modeSubject.next(mode);
  }
}
